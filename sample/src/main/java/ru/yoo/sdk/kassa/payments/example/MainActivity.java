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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import ru.yoo.kassa.payments.example.PaymentMethodTypes;
import ru.yoo.sdk.kassa.payments.checkoutParameters.Amount;
import ru.yoo.sdk.kassa.payments.Checkout;
import ru.yoo.sdk.kassa.payments.ui.color.ColorScheme;
import ru.yoo.sdk.kassa.payments.checkoutParameters.GooglePayParameters;
import ru.yoo.sdk.kassa.payments.checkoutParameters.MockConfiguration;
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentMethodType;
import ru.yoo.sdk.kassa.payments.checkoutParameters.PaymentParameters;
import ru.yoo.sdk.kassa.payments.checkoutParameters.TestParameters;
import ru.yoo.sdk.kassa.payments.TokenizationResult;
import ru.yoo.sdk.kassa.payments.checkoutParameters.UiParameters;
import ru.yoo.sdk.kassa.payments.example.settings.Settings;
import ru.yoo.sdk.kassa.payments.example.settings.SettingsActivity;
import ru.yoo.sdk.kassa.payments.example.utils.AmountFormatter;

/**
 * All calls to MSDK library are handled through the Checkout class.
 *
 * @see Checkout
 */
public final class MainActivity extends AppCompatActivity {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999.99");
    private static final Currency RUB = Currency.getInstance("RUB");
    private static final String KEY_AMOUNT = "amount";
    private static final int REQUEST_CODE_TOKENIZE = 33;

    @Nullable
    private View buyButton;
    @NonNull
    private BigDecimal amount = BigDecimal.ZERO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        initUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Receive token from mSDK

        if (requestCode == REQUEST_CODE_TOKENIZE) {
            switch (resultCode) {
                case RESULT_OK:
                    // successful tokenization
                    final TokenizationResult result = Checkout.createTokenizationResult(data);
                    startActivity(SuccessTokenizeActivity.createIntent(
                            this, result.getPaymentToken(), result.getPaymentMethodType().name()));
                    break;
                case RESULT_CANCELED:
                    // user canceled tokenization
                    Toast.makeText(this, R.string.tokenization_canceled, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (validateAmount()) {
            saveAmount();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_settings == item.getItemId()) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        setContentView(R.layout.activity_main);

        setTitle(R.string.main_toolbar_title);

        final ScrollView scrollContainer = findViewById(R.id.scroll_container);
        if (scrollContainer != null) {
            scrollContainer.post(() -> scrollContainer.fullScroll(ScrollView.FOCUS_DOWN));
        }

        buyButton = findViewById(R.id.buy);
        buyButton.setOnClickListener(v -> onPaymentButtonClick());

        final EditText priceEdit = findViewById(R.id.price);
        priceEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        priceEdit.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceEdit.addTextChangedListener(new AmountFormatter(priceEdit, this::onAmountChange, RUB, MAX_AMOUNT));
        priceEdit.setOnEditorActionListener((v, actionId, event) -> {
            final boolean isActionDone = actionId == EditorInfo.IME_ACTION_DONE;
            if (isActionDone) {
                onPaymentButtonClick();
            }
            return isActionDone;
        });

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        priceEdit.setText(sp.getString(KEY_AMOUNT, BigDecimal.ZERO.toString()));
    }

    private void onPaymentButtonClick() {
        if (validateAmount()) {
            final Settings settings = new Settings(this);
            final Set<PaymentMethodType> paymentMethodTypes = PaymentMethodTypes.getPaymentMethodTypes(settings);

            final PaymentParameters paymentParameters = new PaymentParameters(
                    new Amount(amount, RUB),
                    getString(R.string.main_product_name),
                    getString(R.string.main_product_description),
                    BuildConfig.MERCHANT_TOKEN,
                    BuildConfig.SHOP_ID,
                    settings.getSavePaymentMethod(),
                    paymentMethodTypes,
                    BuildConfig.GATEWAY_ID,
                    getString(R.string.test_redirect_url),
                    settings.autofillUserPhoneNumber() ? getString(R.string.test_phone_number) : null,
                    new GooglePayParameters(),
                    BuildConfig.CLIENT_ID
            );

            final UiParameters uiParameters = new UiParameters(
                    settings.showCheckoutLogo(), new ColorScheme(settings.getPrimaryColor()));

            final MockConfiguration mockConfiguration;
            if (settings.isTestModeEnabled()) {
                mockConfiguration = new MockConfiguration(settings.shouldCompletePaymentWithError(),
                        settings.isPaymentAuthPassed(),
                        settings.getLinkedCardsCount(),
                        new Amount(new BigDecimal(settings.getServiceFee()), RUB));
            } else {
                mockConfiguration = null;
            }

            final TestParameters testParameters = new TestParameters(true, true, mockConfiguration);

            // Start MSDK to get payment token

            final Intent intent = Checkout.createTokenizeIntent(this,
                    paymentParameters,
                    testParameters,
                    uiParameters
            );
            startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
        }
    }

    private void onAmountChange(@NonNull BigDecimal newAmount) {
        amount = newAmount;

        if (buyButton != null) {
            buyButton.setEnabled(validateAmount());
        }
    }

    private void saveAmount() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(KEY_AMOUNT, amount.toPlainString())
                .apply();
    }

    private boolean validateAmount() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
