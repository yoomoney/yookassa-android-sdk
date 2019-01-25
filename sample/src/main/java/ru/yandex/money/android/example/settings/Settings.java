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

package ru.yandex.money.android.example.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public final class Settings {

    public static final String KEY_LINKED_CARDS_COUNT = "linked_cards_count";
    static final String KEY_YANDEX_MONEY_ALLOWED = "yandex_money_allowed";
    static final String KEY_SBERBANK_ONLINE_ALLOWED = "sberbank_online_allowed";
    static final String KEY_GOOGLE_PAY_ALLOWED = "google_pay_allowed";
    static final String KEY_NEW_CARD_ALLOWED = "new_card_allowed";
    static final String KEY_SHOW_YANDEX_CHECKOUT_LOGO = "show_yandex_checkout_logo";
    static final String KEY_TEST_MODE_ENABLED = "test_mode_enabled";
    static final String KEY_PAYMENT_AUTH_PASSED = "payment_auth_passed";
    static final String KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR = "should_complete_with_error";

    private SharedPreferences sp;

    public Settings(@NonNull Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isYandexMoneyAllowed() {
        return sp.getBoolean(KEY_YANDEX_MONEY_ALLOWED, true);
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

    public boolean showYandexCheckoutLogo() {
        return sp.getBoolean(KEY_SHOW_YANDEX_CHECKOUT_LOGO, true);
    }

    public boolean isTestModeEnabled() {
        return sp.getBoolean(KEY_TEST_MODE_ENABLED, false);
    }

    public boolean isPaymentAuthPassed() {
        return sp.getBoolean(KEY_PAYMENT_AUTH_PASSED, false);
    }

    public int getLinkedCardsCount() {
        return sp.getInt(KEY_LINKED_CARDS_COUNT, 1);
    }

    public boolean shouldCompletePaymentWithError() {
        return sp.getBoolean(KEY_SHOULD_COMPLETE_PAYMENT_WITH_ERROR, false);
    }
}
