## Changelog

### 4.0.0
2020-10-20
- Replace appcompat libraries with androidx
- Replace YandexAuthSdk with YooMoney auth-sdk 

### 3.1.0
2020-09-11
- minSdkVersion changed to 21 (Android 5)
- The measure to accept expired cards was rolled back
- Update README.

### 3.0.4
2020-07-24
- Bug fixes and improvements
- Fix scanning of Bank cards on some devices in demo app

### 3.0.3
2020-04-8
- Temporary measure: accepting expired cards (card expiry date must not be earlier than 2020-01-01)
- Update README.
- Update CODEOWNERS.

### 3.0.2
2020-01-23
- Fix minor crashes.

### 3.0.1
2019-12-20
- Update translations.

### 3.0.0
2019-12-18
- Change public api. PaymentParameters and SavedBankCardPaymentParameters now have new required parameter - savePaymentMethod.
- Add ability to save payment methods for recurring payments.

### 2.4.1
2019-11-15
- Fix screen artifacts on closing mSDK.

### 2.4.0
2019-09-06
- Add bank icons for card screen.
- Add tokenization flow for saved bank cards.

### 2.3.0
2019-08-08
- Add info about the fee on the contract screen.
- Fix 3-DS screen closing after Google Pay tokenization.
- Fix keyboard behaviour on user logout.
- Fix scrolling on bank card screen.
- Fix rare crashes after saving instance state.

### 2.2.1
2019-05-16
- ThreatMetrix fix.

### 2.2.0
2019-04-25
- Add new parameter "userPhoneNumber" in PaymentParameters.
- Add setting of supported card networks in Google Pay.
- Fix crash on 3-DS screen (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/26).
- Update README.

### 2.1.0
2019-04-01
- Add color customization.
- Add support for AndroidX (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/21).
- Fix Google Pay disabling from AndroidManifest (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/20).

### 2.0.0
2019-01-25
- Change public api. Refactor Checkout methods and public classes.
- Add javadoc for public classes.
- Add logging. Logging can be turned on in TestParameters object.
- Change 3ds process. Built-in 3ds screen now uses an internal redirect url. If you want to use your own 3ds screen, you can specify customReturnUrl in PaymentParameters object.
- Update README.

### 1.3.0.7
2019-01-24
- Add PAN_ONLY cards from Google Pay as payment option.

### 1.3.0.6
2018-12-27
- Fix 3ds for test payments (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/9).

### 1.3.0.5
2018-12-18
- Fix memory leak.
- Fix SSL handshake problem on Android 4.

### 1.3.0.4
2018-11-23
- ThreatMetrix fix.

### 1.3.0.3
2018-08-15
- Fix crash on bank card screen when user clicks "done" without filling in a card number (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/5).
- Fix keyboard opening automatically after MSDK dismiss (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/3).

### 1.3.0.2
2018-08-02
- Fix 3ds screen crash (https://github.com/yandex-money/yandex-checkout-android-sdk/issues/4).

### 1.3.0.1
2018-07-17
- Fix typos in README.
- Change MSDK behavior when only one payment option is available.

### 1.3.0
Initial public version.