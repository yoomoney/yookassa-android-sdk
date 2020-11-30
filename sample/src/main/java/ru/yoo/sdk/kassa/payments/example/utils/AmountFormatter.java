/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoo.sdk.kassa.payments.example.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Currency;

public final class AmountFormatter implements TextWatcher {

    private static final String DEFAULT_STRING = "0";

    private static final char DECIMAL_SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Nullable
    private AmountListener listener;
    @Nullable
    private Currency currency;

    private boolean formatting;
    private String previousFormattedString;

    @NonNull
    private EditText editText;

    @NonNull
    private BigDecimal limit;

    private boolean cursorAtStartPosition;
    private boolean forcePositionCalculation;
    private boolean forcePositionAfterFirstSymbol;

    public AmountFormatter(@NonNull EditText editText,
                           @Nullable AmountListener listener,
                           @Nullable Currency currency,
                           @NonNull BigDecimal limit) {
        this.editText = editText;
        this.listener = listener;
        this.currency = currency;
        this.limit = limit;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!formatting) {
            cursorAtStartPosition = isInsertingBeforeDigits(s, start, count, after);
            forcePositionCalculation = isDeletingLastSymbol(s, start, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // does nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (formatting) {
            return;
        }

        String currentString = s.toString();
        int currentPosition = editText.getSelectionEnd();

        String numericString = extractNumericString(currentString);

        int lastIndex = numericString.length() - 1;
        if (isNullDeletionRequired(numericString, lastIndex)) {
            // if user entered '.' we should change string to '0.' for further proper formatting
            if (numericString.charAt(lastIndex - 1) == '.') {
                numericString = "0.";
                currentPosition = currentString.length();
            } else {
                numericString = numericString.substring(0, lastIndex);
                forcePositionAfterFirstSymbol = true;
            }
        }

        CharSequence amountFormatted = isSurplus(numericString) ?
                formatAmount(numericString) : formatAmount(extractNumericString(previousFormattedString));

        formatting = true;
        s.replace(0, s.length(), amountFormatted);
        formatting = false;

        String amountFormattedString = amountFormatted.toString();

        final boolean shouldCalculatePosition =
                !amountFormattedString.equals(previousFormattedString) || forcePositionCalculation;
        if (shouldCalculatePosition) {
            calculateCursorPosition(s, currentString, currentPosition, amountFormatted, amountFormattedString);
        }
    }

    private void calculateCursorPosition(Editable s, String currentString, int currentPosition, CharSequence amountFormatted, String amountFormattedString) {
        forcePositionCalculation = false;
        int positionToSet;
        if (forcePositionAfterFirstSymbol) {
            positionToSet = Strings2.getFirstDigitIndex(amountFormatted) + 1;
            forcePositionAfterFirstSymbol = false;
        } else {
            positionToSet = Strings2.getCursorPositionAfterFormat(amountFormattedString,
                    currentString, currentPosition);

            // if user was entering before separator, we should correct position by 1
            if (currentPosition < currentString.length() &&
                    currentString.charAt(currentPosition) == DECIMAL_SEPARATOR) {
                positionToSet--;
            }

            if (positionToSet == s.length()) {
                positionToSet = getPositionToSetEncounteringSeparator(amountFormattedString, positionToSet);
            }
        }
        if (positionToSet >= 0 && positionToSet <= s.length()) {
            editText.setSelection(positionToSet);
        }

        previousFormattedString = amountFormattedString;
    }

    @NonNull
    private CharSequence formatAmount(@NonNull String rawString) {
        try {
            final BigDecimal amount = new BigDecimal(rawString);

            if (listener != null) {
                listener.onAmountValidated(amount.setScale(2, BigDecimal.ROUND_FLOOR));
            }

            final String[] sumSplits = rawString.split("\\.");

            final int fractions;

            if (sumSplits.length == 1 && rawString.contains(".")) {
                fractions = 0;
            } else if (sumSplits.length == 1) {
                fractions = -1;
            } else {
                fractions = Math.min(sumSplits[1].length(), 2);
            }

            return Currencies.formatAsUserInput(amount, currency, fractions);
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    private boolean isInsertingBeforeDigits(@NonNull CharSequence s, int start, int count, int after) {
        return count == 0 && after > 0 && start <= Strings2.getFirstDigitIndex(s);
    }

    private boolean isDeletingLastSymbol(@NonNull CharSequence s, int start, int after) {
        return start == s.length() - 1 && after == 0;
    }

    private boolean isNullDeletionRequired(@NonNull String numericString, int lastIndex) {
        return lastIndex == 1 && numericString.charAt(lastIndex) == '0' && cursorAtStartPosition;
    }

    private int getPositionToSetEncounteringSeparator(@NonNull String amountFormattedString, int positionToSet) {
        boolean digitFound = false;
        while (!digitFound && positionToSet > 0) {
            char character = amountFormattedString.charAt(positionToSet - 1);
            if (Character.isDigit(character) || character == DECIMAL_SEPARATOR) {
                digitFound = true;
            } else positionToSet--;
        }
        return positionToSet;
    }

    @NonNull
    private String extractNumericString(@NonNull String sumString) {
        StringBuilder stringBuilder = new StringBuilder(sumString.
                replaceAll(String.format("[^\\d^%s^.]", DECIMAL_SEPARATOR), "").
                replace(DECIMAL_SEPARATOR, '.'));

        int separatorIndex = stringBuilder.indexOf(".");
        for (int i = separatorIndex + 1; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '.') {
                stringBuilder.deleteCharAt(i);
            }
        }
        String extractedString = stringBuilder.toString();

        return extractedString.isEmpty() || ".".equals(extractedString) ? DEFAULT_STRING : extractedString;
    }

    private boolean isSurplus(@NonNull String numericString) {
        return new BigDecimal(numericString).compareTo(limit) <= 0;
    }

    public interface AmountListener {
        void onAmountValidated(@NonNull BigDecimal amount);
    }
}
