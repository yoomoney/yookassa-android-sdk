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

package ru.yoo.sdk.kassa.payments.example.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.yoo.sdk.kassa.payments.example.BuildConfig;
import ru.yoo.sdk.kassa.payments.example.R;
import ru.yoo.sdk.kassa.payments.Checkout;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_AUTOFILL_USER_PHONE_NUMBER;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_GOOGLE_PAY_ALLOWED;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_NEW_CARD_ALLOWED;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_PAYMENT_AUTH_PASSED;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SAVE_PAYMENT_METHOD;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SBERBANK_ONLINE_ALLOWED;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_SHOW_CHECKOUT_LOGO;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_TEST_MODE_ENABLED;
import static ru.yoo.sdk.kassa.payments.example.settings.Settings.KEY_YOO_MONEY_ALLOWED;
import static ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_CODE;
import static ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_DESCRIPTION;
import static ru.yoo.sdk.kassa.payments.Checkout.EXTRA_ERROR_FAILING_URL;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int START_CONFIRMATION_REQUEST_CODE = 1;

    private List<Pair<String, Integer>> keysToIds = Arrays.asList(
            new Pair<>(KEY_YOO_MONEY_ALLOWED, R.id.payment_option_yoomoney),
            new Pair<>(KEY_SBERBANK_ONLINE_ALLOWED, R.id.payment_option_sberbank_online),
            new Pair<>(KEY_NEW_CARD_ALLOWED, R.id.payment_option_new_card),
            new Pair<>(KEY_SHOW_CHECKOUT_LOGO, R.id.checkout_logo),
            new Pair<>(KEY_AUTOFILL_USER_PHONE_NUMBER, R.id.enable_default_user_phone_number),
            new Pair<>(KEY_TEST_MODE_ENABLED, R.id.enable_test_mode),
            new Pair<>(KEY_PAYMENT_AUTH_PASSED, R.id.payment_auth_passed),
            new Pair<>(KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR, R.id.complete_with_error)
    );
    private View linkedCardsButton;
    private View feeButton;
    private CompoundButton paymentAuthPassedSwitch;
    private CompoundButton completeWithErrorButton;
    private View paymentAuthPassedDivider;
    private View linkedCardsDivider;
    private View feeDivider;
    private View completeWithErrorDivider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));

        linkedCardsButton = findViewById(R.id.linked_cards);
        linkedCardsButton.setOnClickListener(this);

        feeButton = findViewById(R.id.fee);
        feeButton.setOnClickListener(this);

        findViewById(R.id.colorScheme).setOnClickListener(
                v -> startActivity(new Intent(SettingsActivity.this, ColorSchemeActivity.class)));

        final CompoundButton enableTestModeSwitch = findViewById(R.id.enable_test_mode);
        paymentAuthPassedSwitch = findViewById(R.id.payment_auth_passed);
        completeWithErrorButton = findViewById(R.id.complete_with_error);
        paymentAuthPassedDivider = findViewById(R.id.payment_auth_passed_divider);
        linkedCardsDivider = findViewById(R.id.linked_cards_divider);
        feeDivider = findViewById(R.id.fee_divider);
        completeWithErrorDivider = findViewById(R.id.complete_with_error_divider);
        final Spinner savePaymentMethodSelector = findViewById(R.id.savePaymentMethodSelector);

        enableTestModeSwitch.setOnCheckedChangeListener(
                (v, isChecked) -> setTestGroupVisibility(isChecked ? View.VISIBLE : View.GONE));

        setTestGroupVisibility(enableTestModeSwitch.isChecked() ? View.VISIBLE : View.GONE);

        final Settings settings = new Settings(this);

        this.<CompoundButton>findViewById(R.id.payment_option_yoomoney).setChecked(settings.isYooMoneyAllowed());
        this.<CompoundButton>findViewById(R.id.payment_option_new_card).setChecked(settings.isNewCardAllowed());
        this.<CompoundButton>findViewById(R.id.payment_option_sberbank_online)
                .setChecked(settings.isSberbankOnlineAllowed());
        this.<CompoundButton>findViewById(R.id.checkout_logo)
                .setChecked(settings.showCheckoutLogo());
        this.<CompoundButton>findViewById(R.id.enable_default_user_phone_number)
                .setChecked(settings.autofillUserPhoneNumber());
        enableTestModeSwitch.setChecked(settings.isTestModeEnabled());
        paymentAuthPassedSwitch.setChecked(settings.isPaymentAuthPassed());
        completeWithErrorButton.setChecked(settings.shouldCompletePaymentWithError());

        setUpSavePaymentMethodSelector(savePaymentMethodSelector, settings);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(BuildConfig.BUILD_DATE);

        final String versionInfo = getString(
                R.string.version_template, BuildConfig.VERSION_NAME, calendar, BuildConfig.VERSION_CODE);
        this.<TextView>findViewById(R.id.version_info).setText(versionInfo);

        final View open3dsButton = findViewById(R.id.open3ds);
        if (BuildConfig.DEBUG) {
            open3dsButton.setVisibility(View.VISIBLE);
            open3dsButton.setOnClickListener(
                    v -> {
                        try {
                            startActivityForResult(
                                    Checkout.create3dsIntent(this, "https://yoomoney.ru/api-pages/v3/3ds?acsUri=https%3A%2F%2Fdemo-scrat.yamoney.ru%3A8443%2Fmerchant-test-card-stub%2F3ds%3Ftes11%3D1%26amp%3Btest2%3D2&MD=1536663442128-3079210364026365123&PaReq=Q1VSUkVOQ1k9UlVSJlRFUk1JTkFMPTk5OTk5OCZFWFBfWUVBUj0yNSZQX1NJR049M2E3MmM2MGNhMjIyNjU4MTJhOTgwNmUzMjFmMTQyZTQ1NzVjMmQ0OCZSRVNQT05TRV9SUk49MjEwMzYzOTA5MzExJkVNQUlMPW5vcmVwbHklNDBtb25leS55YW5kZXgucnUmTUVSQ0hfTkFNRT1ZTSZERVNDPTIzMjliMzUxLTAwMGYtNTAwMC1hMDAwLTFlYTA0NWQzZjc5MSZSRVNQT05TRV9BVVRIQ09ERT05MDUwNTMmTUVSQ0hBTlQ9NzkwMzY3Njg2MjE5OTk5JkNBUkQ9MTExMTExMTExMTExMTAyNiZOQU1FPSZPUkRFUj01MDk2MzU0MTYyJk1FUkNIX1VSTD1tb25leS55YW5kZXgucnUmQU1PVU5UPTEuMDAmQkFDS1JFRj1odHRwJTNBJTJGJTJGcmVkaXJlY3QudXJsLmNvbSUyRiZUSU1FU1RBTVA9MjAxODA5MTExMDU3MjImVFJUWVBFPTAmRVhQPTEyJkNWQzI9MTIzJk5PTkNFPTEyMzQ1Njc4OTBBQkNERUZlMjNhZGNlNTg4OGE4&TermUrl=https%3A%2F%2Fpaymentcard.yamoney.ru%3A443%2Fgates%2Fmb3dsdemoprovider"),
                                    START_CONFIRMATION_REQUEST_CODE);
                        } catch (IllegalStateException e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            savePreferences();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        savePreferences();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bundle extras = data == null ? null : data.getExtras();

        if (requestCode == START_CONFIRMATION_REQUEST_CODE && extras != null) {
            final String message;

            if (resultCode == RESULT_OK) {
                message = "Result OK";
            } else {
                message = "errorCode=" + extras.get(EXTRA_ERROR_CODE) + "\n" +
                        "errorDescription=" + extras.get(EXTRA_ERROR_DESCRIPTION) + "\n" +
                        "errorFailingUrl=" + extras.get(EXTRA_ERROR_FAILING_URL);
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.linked_cards) {
            startActivity(new Intent(this, LinkedCardsActivity.class));
        } else if (v.getId() == R.id.fee) {
            startActivity(new Intent(this, FeeActivity.class));
        }
    }

    private void setUpSavePaymentMethodSelector(Spinner savePaymentMethodSelector, Settings settings) {
        String[] items = new String[]{
                getString(R.string.save_payment_method_selector_user_decide),
                getString(R.string.save_payment_method_selector_save),
                getString(R.string.save_payment_method_selector_dont_save)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        savePaymentMethodSelector.setAdapter(adapter);
        savePaymentMethodSelector.setSelection(settings.getSavePaymentMethodId());
        savePaymentMethodSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                        .putInt(KEY_SAVE_PAYMENT_METHOD, position)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setTestGroupVisibility(int visibility) {
        paymentAuthPassedSwitch.setVisibility(visibility);
        feeButton.setVisibility(visibility);
        linkedCardsButton.setVisibility(visibility);
        completeWithErrorButton.setVisibility(visibility);
        paymentAuthPassedDivider.setVisibility(visibility);
        linkedCardsDivider.setVisibility(visibility);
        completeWithErrorDivider.setVisibility(visibility);
        feeDivider.setVisibility(visibility);
    }

    private void savePreferences() {
        final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();

        for (Pair<String, Integer> keyToId : keysToIds) {
            edit.putBoolean(keyToId.first, this.<CompoundButton>findViewById(keyToId.second).isChecked());
        }

        edit.apply();
    }
}
