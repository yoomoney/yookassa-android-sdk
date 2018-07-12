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

package ru.yandex.money.android.example.linkedcards;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.TextView;
import ru.yandex.money.android.example.R;
import ru.yandex.money.android.example.settings.Settings;

public final class LinkedCardsActivity extends AppCompatActivity {

    private static final int MAX_CARDS = 5;
    private static final String KEY_LAST_COUNT = "lastCount";

    static final String TEXT_ZERO = "0";
    static final String TEXT_MAX = "5";

    private int lastCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_cards);

        this.<TextView>findViewById(R.id.cards_count).addTextChangedListener(new CountTextWatcher());

        if (savedInstanceState == null) {
            lastCount = new Settings(this).getLinkedCardsCount();
        } else {
            lastCount = savedInstanceState.getInt(KEY_LAST_COUNT);
        }

        this.<TextView>findViewById(R.id.cards_count).setText(String.valueOf(lastCount));
        findViewById(R.id.save).setOnClickListener(v -> onSave());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_LAST_COUNT, lastCount);
    }

    void onSave() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(Settings.KEY_LINKED_CARDS_COUNT, lastCount)
                .apply();

        finish();
    }

    private class CountTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // does nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // does nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                final int parsed = Integer.parseInt(s.toString());
                if (parsed < 0 || parsed > MAX_CARDS || s.length() > 1) {
                    s.replace(0, s.length(), String.valueOf(lastCount));
                } else {
                    lastCount = parsed;
                }
            } catch (NumberFormatException e) {
                s.replace(0, s.length(), String.valueOf(lastCount));
            }

            Selection.selectAll(s);
        }
    }
}
