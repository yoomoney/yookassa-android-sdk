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

package ru.yandex.money.android.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public final class SuccessTokenizeActivity extends AppCompatActivity {

    public static final String TOKEN_EXTRA = "paymentToken";
    public static final String TYPE_EXTRA = "type";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String paymentToken, @NonNull String type) {
        return new Intent(context, SuccessTokenizeActivity.class)
                .putExtra(SuccessTokenizeActivity.TOKEN_EXTRA, paymentToken)
                .putExtra(SuccessTokenizeActivity.TYPE_EXTRA, type);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_tokenize);

        findViewById(R.id.close).setOnClickListener(v -> finish());

        findViewById(R.id.showDocumentation).setOnClickListener(v -> {
            final Intent showDoc = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.checkout_documentation_link)));
            startActivity(showDoc);
        });

        findViewById(R.id.showToken).setOnClickListener(v -> {
            final Intent intent = getIntent();
            final String token = intent.getStringExtra(TOKEN_EXTRA);
            final String type = intent.getStringExtra(TYPE_EXTRA);
            new AlertDialog.Builder(this, R.style.DialogToken)
                    .setMessage("Token: " + token + "\nType: " + type)
                    .setPositiveButton(R.string.token_copy, (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData clip = ClipData.newPlainText("token", token);
                            clipboard.setPrimaryClip(clip);
                        }
                    })
                    .setNegativeButton(R.string.token_cancel, (dialog, which) -> {
                    })
                    .show();
        });
    }
}

