/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.example.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

public final class Currencies {

    private static final char RUBLE_SIGN = '\u20BD';

    private static final Currency RUB = Currency.getInstance("RUB");

    private Currencies() {
    }

    @NonNull
    private static CharSequence format(@Nullable BigDecimal value, @Nullable Currency currency,
                                       @IntRange(from = 0) int maximumFractionDigits) {

        if (value == null) {
            value = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = RUB;
        }

        NumberFormat numberFormat = getNumberFormat(currency);
        numberFormat.setRoundingMode(RoundingMode.FLOOR);
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);

        String formatted = numberFormat.format(value);
        if (currency == RUB) {
            SpannableStringBuilder builder = new SpannableStringBuilder(formatted)
                    .append(' ');
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                builder.append('Я');
                builder.setSpan(RoubleTypefaceSpan.instance, builder.length() - 1, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.append(RUBLE_SIGN);
            }
            return builder;
        } else {
            return formatted;
        }
    }

    /**
     * Format {@code value} as currency string using {@link Currency} and fraction digits as if user enters
     * symbols one by one
     *
     * @param value             value to format
     * @param currency          {@link Currency} object
     * @param fractionDigits    fraction digits to display. Should be in range from -1 to 2. -1 means no fractional
     *                          digits with no separator, 0 means only separator and [1, 2] means 1 or 2 fractional
     *                          digits respectively.
     * @return                  formatted string
     */
    @NonNull
    public static CharSequence formatAsUserInput(@Nullable BigDecimal value, @Nullable Currency currency,
                                                 @IntRange(from = -1, to = 2) int fractionDigits) {
        if (fractionDigits == 0) {
            CharSequence formatted = format(value, currency, 1);
            String formattedString = formatted.toString();
            int decimalSeparatorIndex = formattedString.indexOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());

            int end = decimalSeparatorIndex + 2;
            if (decimalSeparatorIndex == -1 || end > formatted.length()) {
                return formatted;
            }

            int start = decimalSeparatorIndex + 1;
            return new SpannableStringBuilder(formatted).replace(start, end, "");
        } else {
            return format(value, currency, Math.max(0, fractionDigits));
        }
    }

    @NonNull
    private static NumberFormat getDefaultCurrencyFormat() {
        return new DecimalFormat("#,##0.00");
    }

    @NonNull
    private static NumberFormat getNumberFormat(@NonNull Currency currency) {
        // because we want to append currency sign manually
        if (RUB.equals(currency)) {
            return getDefaultCurrencyFormat();
        } else {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
            numberFormat.setCurrency(currency);
            return numberFormat;
        }
    }

    public static final class RoubleTypefaceSpan extends MetricAffectingSpan {

        private static final float ROUBLE_SCREW_X = -0.25f;

        static RoubleTypefaceSpan instance;

        private final Typeface typeface;

        private RoubleTypefaceSpan(@NonNull Context context) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/rouble.otf");
        }

        public static void init(@NonNull Context context) {
            instance = new RoubleTypefaceSpan(context.getApplicationContext());
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            update(p);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            update(tp);
        }

        private void update(@NonNull TextPaint paint) {
            final Typeface paintTypeface = paint.getTypeface();
            final int style = paintTypeface == null ? 0 : paintTypeface.getStyle();

            if ((style & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((style & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(ROUBLE_SCREW_X);
            }

            paint.setTypeface(typeface);
        }
    }
}
