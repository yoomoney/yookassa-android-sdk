/*
 * The MIT License (MIT)
 * Copyright © 2019 NBCO Yandex.Money LLC
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

package ru.yandex.money.android.example.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ru.yandex.money.android.example.R;

public final class ColorSchemeActivity extends AppCompatActivity {

    private EditText redValue;
    private EditText greenValue;
    private EditText blueValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_scheme);

        this.<Toolbar>findViewById(R.id.toolbar).setNavigationOnClickListener(v -> finish());

        redValue = findViewById(R.id.red_value);
        greenValue = findViewById(R.id.green_value);
        blueValue = findViewById(R.id.blue_value);

        if (savedInstanceState == null) {
            final int primaryColor = new Settings(this).getPrimaryColor();
            redValue.setText(String.valueOf(Color.red(primaryColor)));
            greenValue.setText(String.valueOf(Color.green(primaryColor)));
            blueValue.setText(String.valueOf(Color.blue(primaryColor)));
        }

        findViewById(R.id.save).setOnClickListener(v -> onSave());
    }

    void onSave() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(Settings.KEY_PRIMARY_COLOR_RED_VALUE, Integer.valueOf(redValue.getText().toString()))
                .putInt(Settings.KEY_PRIMARY_COLOR_GREEN_VALUE, Integer.valueOf(greenValue.getText().toString()))
                .putInt(Settings.KEY_PRIMARY_COLOR_BLUE_VALUE, Integer.valueOf((blueValue.getText().toString())))
                .apply();

        finish();
    }
}
