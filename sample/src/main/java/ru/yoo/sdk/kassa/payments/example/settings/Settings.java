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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import ru.yoo.sdk.kassa.payments.checkoutParameters.SavePaymentMethod;

public final class Settings {

    static final String KEY_LINKED_CARDS_COUNT = "linked_cards_count";
    static final String KEY_PRIMARY_COLOR_RED_VALUE = "primary_color_red_value";
    static final String KEY_PRIMARY_COLOR_GREEN_VALUE = "primary_color_green_value";
    static final String KEY_PRIMARY_COLOR_BLUE_VALUE = "primary_color_blue_value";
    static final String KEY_YOO_MONEY_ALLOWED = "yoo_money_allowed";
    static final String KEY_SBERBANK_ONLINE_ALLOWED = "sberbank_online_allowed";
    static final String KEY_GOOGLE_PAY_ALLOWED = "google_pay_allowed";
    static final String KEY_NEW_CARD_ALLOWED = "new_card_allowed";
    static final String KEY_SHOW_CHECKOUT_LOGO = "show_yoo_checkout_logo";
    static final String KEY_AUTOFILL_USER_PHONE_NUMBER = "autofill_user_phone_number";
    static final String KEY_TEST_MODE_ENABLED = "test_mode_enabled";
    static final String KEY_PAYMENT_AUTH_PASSED = "payment_auth_passed";
    static final String KEY_SERVICE_FEE = "fee";
    static final String KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR = "should_complete_with_error";
    static final String KEY_SAVE_PAYMENT_METHOD = "save_payment_method";

    private SharedPreferences sp;

    public Settings(@NonNull Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isYooMoneyAllowed() {
        return sp.getBoolean(KEY_YOO_MONEY_ALLOWED, true);
    }

    public boolean isSberbankOnlineAllowed() {
        return sp.getBoolean(KEY_SBERBANK_ONLINE_ALLOWED, true);
    }

    public boolean isGooglePayAllowed() {
        return sp.getBoolean(KEY_GOOGLE_PAY_ALLOWED, true);
    }

    public boolean isNewCardAllowed() {
        return sp.getBoolean(KEY_NEW_CARD_ALLOWED, true);
    }

    public boolean showCheckoutLogo() {
        return sp.getBoolean(KEY_SHOW_CHECKOUT_LOGO, true);
    }

    public boolean autofillUserPhoneNumber() {
        return sp.getBoolean(KEY_AUTOFILL_USER_PHONE_NUMBER, false);
    }

    public boolean isTestModeEnabled() {
        return sp.getBoolean(KEY_TEST_MODE_ENABLED, false);
    }

    public boolean isPaymentAuthPassed() {
        return sp.getBoolean(KEY_PAYMENT_AUTH_PASSED, false);
    }

    public float getServiceFee() {
        return sp.getFloat(KEY_SERVICE_FEE, 0f);
    }

    public int getLinkedCardsCount() {
        return sp.getInt(KEY_LINKED_CARDS_COUNT, 1);
    }

    SharedPreferences getSp() {
        return sp;
    }

    public SavePaymentMethod getSavePaymentMethod() {
        return getSavePaymentMethod(getSavePaymentMethodId());
    }

    int getSavePaymentMethodId() {
        return sp.getInt(KEY_SAVE_PAYMENT_METHOD, 0);
    }

    @ColorInt
    public int getPrimaryColor() {
        return Color.rgb(
                sp.getInt(KEY_PRIMARY_COLOR_RED_VALUE, 0),
                sp.getInt(KEY_PRIMARY_COLOR_GREEN_VALUE, 168),
                sp.getInt(KEY_PRIMARY_COLOR_BLUE_VALUE, 132)
        );
    }

    public boolean shouldCompletePaymentWithError() {
        return sp.getBoolean(KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR, false);
    }

    private static SavePaymentMethod getSavePaymentMethod(int value) {
        SavePaymentMethod savePaymentMethod;
        switch (value) {
            case 0:
                savePaymentMethod = SavePaymentMethod.USER_SELECTS;
                break;
            case 1:
                savePaymentMethod = SavePaymentMethod.ON;
                break;
            case 2:
                savePaymentMethod = SavePaymentMethod.OFF;
                break;
            default:
                savePaymentMethod = SavePaymentMethod.USER_SELECTS;
        }
        return savePaymentMethod;
    }
}
