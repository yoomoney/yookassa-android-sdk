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

package ru.yoo.sdk.kassa.payments.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ru.yoomoney.sdk.kassa.payments.Checkout;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType;

public final class SuccessTokenizeActivity extends AppCompatActivity {

    public static final String TOKEN_EXTRA = "paymentToken";
    public static final String PAYMENT_TYPE_EXTRA = "payment_type";
    public static final int REQUEST_CODE_3DS = 34;
    public static final int REQUEST_CODE_APP_2_APP = 36;

    @NonNull
    private PaymentMethodType paymentMethodType = PaymentMethodType.BANK_CARD;

    @NonNull
    private String url3ds = "";

    @NonNull
    private String urlApp2App = "";

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull String paymentToken,
            @NonNull String paymentType
    ) {
        return new Intent(context, SuccessTokenizeActivity.class)
                .putExtra(SuccessTokenizeActivity.TOKEN_EXTRA, paymentToken)
                .putExtra(SuccessTokenizeActivity.PAYMENT_TYPE_EXTRA, paymentType);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_tokenize);

        final Intent intent = getIntent();
        final String token = intent.getStringExtra(TOKEN_EXTRA);
        final String paymentType = intent.getStringExtra(PAYMENT_TYPE_EXTRA);
        paymentMethodType = PaymentMethodType.valueOf(paymentType);

        findViewById(R.id.close).setOnClickListener(v -> finish());
        findViewById(R.id.showDocumentation).setOnClickListener(v -> openLink(R.string.checkout_documentation_link));
        findViewById(R.id.showGithub).setOnClickListener(v -> openLink(R.string.checkout_github_link));

        int show3dsContainer = shouldShow3dsContainer();
        findViewById(R.id.container3ds).setVisibility(show3dsContainer);
        if (show3dsContainer == View.VISIBLE) {
            findViewById(R.id.confirm).setOnClickListener(v -> open3dsConfirmation());
            ((EditText) findViewById(R.id.url3ds)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    url3ds = s.toString();
                }
            });
        }

        int showApp2AppContainer = shouldShowApp2AppContainer();
        findViewById(R.id.containerApp2App).setVisibility(showApp2AppContainer);
        if (show3dsContainer == View.VISIBLE) {
            findViewById(R.id.confirmApp2App).setOnClickListener(v -> openApp2AppConfirmation());
            ((EditText) findViewById(R.id.urlApp2App)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    urlApp2App = s.toString();
                }
            });
        }


        findViewById(R.id.showToken).setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.ym_DialogStyle)
                    .setMessage("Token: " + token + "\nPayment type: " + paymentType)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_3DS) {
            switch (resultCode) {
                case RESULT_OK:
                    showConfirmationAlertDialog(getString(R.string.success_3ds));
                    break;
                case RESULT_CANCELED:
                    showConfirmationAlertDialog(getString(R.string.cancel_3ds));
                    break;
                case Checkout.RESULT_ERROR:
                    String error =
                            getString(R.string.error_code_3ds) +
                                    String.valueOf(data.getIntExtra(Checkout.EXTRA_ERROR_CODE, -1)) + "\n" +
                                    getString(R.string.error_description_3ds) +
                                    data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION) + "\n" +
                                    getString(R.string.error_failing_url_3ds) +
                                    data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL);
                    showConfirmationAlertDialog(error);
                    break;
            }
        } else if (requestCode == REQUEST_CODE_APP_2_APP) {
            switch (resultCode) {
                case RESULT_OK:
                    showConfirmationAlertDialog(getString(R.string.success_app2app));
                    break;
                case RESULT_CANCELED:
                    showConfirmationAlertDialog(getString(R.string.cancel_app2app));
            }
        }
    }

    private void openLink(int linkResId) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(linkResId))));
    }

    private void showConfirmationAlertDialog(@NonNull String message) {
        new AlertDialog.Builder(this, R.style.ym_DialogStyle)
                .setMessage(message)
                .setPositiveButton(R.string.token_cancel, (dialog, which) -> { })
                .show();
    }

    private void open3dsConfirmation() {
        if (URLUtil.isHttpsUrl(url3ds) || URLUtil.isAssetUrl(url3ds)) {
            Intent intent = Checkout.createConfirmationIntent(this, url3ds, paymentMethodType);
            startActivityForResult(intent, REQUEST_CODE_3DS);
        } else {
            Toast.makeText(this, getString(R.string.error_wrong_url), Toast.LENGTH_SHORT).show();
        }
    }

    private void openApp2AppConfirmation() {
        startActivityForResult(
                Checkout.createConfirmationIntent( this, urlApp2App, PaymentMethodType.SBERBANK),
                REQUEST_CODE_APP_2_APP
        );
    }

    private int shouldShow3dsContainer() {
        return BuildConfig.DEBUG ? View.VISIBLE : View.GONE;
    }

    private int shouldShowApp2AppContainer() {
        return BuildConfig.DEBUG &&
                getIntent().getStringExtra(PAYMENT_TYPE_EXTRA).equals(PaymentMethodType.SBERBANK.name())
                ? View.VISIBLE
                : View.GONE;

    }
}
