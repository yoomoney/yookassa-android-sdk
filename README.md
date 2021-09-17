# YooMoney for Business Payments SDK (YooKassaPayments)

[![Platform](https://img.shields.io/badge/Support-SDK%2021+-brightgreen.svg)](https://img.shields.io/badge/Support-SDK%2021+-brightgreen.svg)
[![GitHub tag](https://img.shields.io/github/v/tag/yoomoney/yookassa-android-sdk.svg?sort=semver)](https://img.shields.io/github/v/tag/yoomoney/yookassa-android-sdk.svg?sort=semver)
[![License](https://img.shields.io/github/license/yoomoney/yookassa-android-sdk.svg)](https://img.shields.io/github/license/yoomoney/yookassa-android-sdk.svg)

This library allows implementing payment acceptance into mobile apps on Android.
It works as an extension to the YooMoney API.

The mobile SDK contains ready-made payment interfaces (the payment form and everything related to it).

Using the SDK, you can receive tokens for processing payments via bank cards, Google Pay, Sberbank Online, or YooMoney wallets.

Oldest supported version of android sdk: 21(Android 5).

This repository contains the SDK code and an example of an app which integrates it.
* [Library code](./library)
* [Code of the demo app which integrates the SDK](./sample)
* [README на русском](./README_RU.md)


#  Documentation

Android Checkout mobile SDK: version $versionName ([changelog](https://github.com/yoomoney/yookassa-android-sdk/blob/master/CHANGELOG.md))

* [Changelog](#changelog)
* [Migration guide](#migration-guide)
* [Registering an app for payments via the wallet](#registering-an-app-for-payments-via-the-wallet)
* [Adding dependencies](#adding-dependencies)
    * [Implementation via Gradle](#implementation-via-Gradle)
    * [Implementing sdk authorization for payments via the wallet](#setting-up-the-app-scheme)
    * [Configuring the app for selling digital products](#configuring-the-app-for-selling-digital-products)
* [Using the library](#using-the-library)
    * [Tokenization](#tokenization)
        * [Launching tokenization](#launching-tokenization)
        * [Launching tokenization for saved bank cards](#launching-tokenization-for-saved-bank-cards)
        * [Getting tokenization results](#getting-tokenization-results)
        * [Using the payment token](#using-the-payment-token)
        * [Test parameters and debugging](#test-parameters-and-debugging)
        * [Interface configuration](#interface-configuration)
    * [3DSecure](#3dsecure)
    * [Scanning bank cards](#scanning-bank-cards)
* [Useful links](#useful-links)

# Changelog

[Link to the Changelog](https://github.com/yoomoney/yookassa-android-sdk/blob/master/CHANGELOG.md)

# Migration guide

[Link to the Migration guide](https://github.com/yoomoney/yookassa-android-sdk/blob/master/MIGRATION.md)

# Registering an app for payments via the wallet
> If the YooMoney wallet is one of the payment methods, you need to register your app and get `authCenterClientId`.
If not, you can skip this step.

If you've already registered apps for **oAuth authorization**, then you can find the list of your apps on this page: https://yookassa.ru/oauth/v2/client
If you haven't registered apps for **oAuth authorization** yet, you need to follow these instructions.
1. To register a new app, you need to sign in at https://yookassa.ru
2. After signing in, go to the page for registering apps: https://yookassa.ru/oauth/v2/client
3. Click on the Create app button and enter values for the following parameters:
    * Name;
    * Description. Optional;
    * Link to the website;
    * Callback URL: any, you can enter the link to the website;
    * Accesses. There are three sections here: `YooMoney API`, `YooMoney wallet`, and `YooMoney account`.
        * Give permission to access user's wallet balance in the `YooMoney wallet` section. To do that, put a check mark next to the **View** field in the**WALLET BALANCE** section;
        * Open the `YooMoney account` section and give permission to access user's phone number, email address, name, and profile picture. To do that, put a check mark next to the **View** field in the **PHONE NUMBER, EMAIL ADDRESS, NAME, AND PROFILE PICTURE** section;
3. Click on the Register button and finish the registration;
4. A window with information about the registered app will open. You'll need `authCenterClientId` to launch tokenization, see [(Launching tokenization)](#launching-tokenization);

# Adding dependencies

## Implementation via Gradle
To implement the library, enter dependencies in build.gradle of the module:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:$versionName'
}
```

Ask your onboarding manager for the `ThreatMetrix Android SDK 5.4-73.aar` library.
Create a libs folder in the module where you implement the sdk and add the `ThreatMetrix Android SDK 5.4-73.aar` there. Add the following in build.gradle of the same module in dependencies:
```groovy
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
}
```

## Setting up the app scheme
In order for the sdk to work, you need to set up your app's scheme for processing deep links. It's required for payments via sberpay.
You need to add `resValue "string", "ym_app_scheme", "your_unique_app_shceme"` to your build.gradle file in the android.defaultConfig section
```
android {
    defaultConfig {
        resValue "string", "ym_app_scheme", "your_unique_app_shceme"
    }
}
```
Or add a row similar to the following example to your strings.xml:
```
<resources>
    <string name="ym_app_scheme" translatable="false">your_unique_app_shceme</string>
</resources>
```
Where your_unique_app_shceme is a unique name of your app; if you already process deep links in your app, you can use your app's working scheme.
If you haven't processed deep links in the project before, you can create a unique scheme made of Latin letters for your app.

## Configuring the app for selling digital products
If your app is used for selling digital products, you need to remove Google Pay from the list of payment methods.
To do that, add the following code to AndroidManifest:

```xml
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    tools:node="remove" />
```

# Using the library

The `ru.yoomoney.sdk.kassa.payments.Checkout` class is used for everything involved in working with the library

## Tokenization

### Launching tokenization

The `Checkout.createTokenizeIntent()` method is used to launch the tokenization process. This method returns `Intent` which should be launched in startActivityForResult(). After that, control over the process will be transferred to the SDK.
You can get a ready-made payment token in `onActivityResult()` (see [Getting tokenization results](#getting-tokenization-results))

Input parameters of the method:
* context (Context): app's context;
* paymentParameters (PaymentParameters): payment parameters;
* testParameters (TestParameters): parameters for debugging, see [Test parameters and debugging](#test-parameters-and-debugging);
* uiParameters (UiParameters): interface settings, see [Interface configuration](#interface-configuration).

`PaymentParameters` fields:

Required parameters:
* amount (Amount): product price. Available payment methods can change depending on this parameter;
* title (String): product name;
* subtitle (String): product description;
* clientApplicationKey (String): key for client apps from the YooMoney Merchant Profile ([Settings section—API keys](https://yookassa.ru/my/api-keys-settings)).;
* shopId (String): store's ID in YooMoney.
* savePaymentMethod (SavePaymentMethod): configuration for saving payment methods. Saved payment methods can be used for processing recurring payments.
* authCenterClientId (String): app's ID for sdk authorization `ru.yoomoney.sdk.auth`, see [Registering an app for payments via the wallet](#registering-an-app-for-payments-via-the-wallet).

Optional parameters:
* paymentMethodTypes (Set of PaymentMethodType): restrictions on payment methods. If you leave the field empty or enter null in it,
the library will use all available payment methods;
* gatewayId (String): gatewayId for the store;
* customReturnUrl (String): url of the page (only https supported) where you need to return after completing 3ds. It should only be used if a custom Activity is used for 3ds url. If Checkout.createConfirmationIntent() or Checkout.create3dsIntent() is used, don't specify this parameter;
* userPhoneNumber (String): user's phone number. It's used for autofilling fields for payments via SberPay. Supported format: "+7XXXXXXXXXX".
* googlePayParameters (GooglePayParameters): settings for payments via Google Pay;
* customerId (String): unique customer id for your system, ex: email or phone number. 200 symbols max. Used by library to save user payment method and display saved methods. It is your responsibility to make sure that a particular customerId identifies the user, which is willing to make a purchase.

Fields of the `Amount` class:
* value (BigDecimal): amount;
* currency (Currency): currency.

Values of `SavePaymentMethod`:
* ON: Save the payment methods for processing recurring payments. Only payment methods which support saving will be available to users. A notification that the payment method will be saved will be displayed on the contract screen.
* OFF: Don't save the payment method.
* USER_SELECTS: User selects if the payment method should be used or not. If a method can be used, a switch will appear on the contract screen.

Values of `PaymentMethodType`:
* YOO_MONEY: the payment was made via the YooMoney wallet;
* BANK_CARD: the payment was made via a bank card;
* SBERBANK: the payment was made via Sberbank (text message invoicing or SberPay);
* GOOGLE_PAY: the payment was made via Google Pay.

Fields of the `GooglePayParameters` class:
* allowedCardNetworks (Set of GooglePayCardNetwork): payment systems which can be used for payments via Google Pay.

Values of `GooglePayCardNetwork`:
* AMEX
* DISCOVER
* JCB
* MASTERCARD
* VISA
* INTERAC
* OTHER

```java
class MyActivity extends AppCompatActivity {

    ...

    void timeToStartCheckout() {
        PaymentParameters paymentParameters = new PaymentParameters(
                new Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
                "Product name",
                "Product description",
                "live_AAAAAAAAAAAAAAAAAAAA",
                "12345",
                SavePaymentMethod.OFF
        );
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
}
```

### Launching tokenization for saved bank cards

This tokenization method is used in case there's a bank card linked to the store and its csc needs to be requested from the user again.
Otherwise, the standard tokenization procedure should be followed (see [Launching tokenization](#launching-tokenization)).

The `Checkout.createSavedCardTokenizeIntent()` method is used to launch the tokenization process with a payment ID. This method returns `Intent` which should be launched in startActivityForResult().
You can get a ready-made payment token in `onActivityResult()` (see [Getting tokenization results](#getting-tokenization-results))

Input parameters of the method:
* context (Context): app's context;
* savedBankCardPaymentParameters (SavedBankCardPaymentParameters): parameters of a payment with a saved bank card;
* testParameters (TestParameters): parameters for debugging, see [Test parameters and debugging](#test-parameters-and-debugging);
* uiParameters (UiParameters): interface settings, see [Interface configuration](#interface-configuration).

Fields of `SavedBankCardPaymentParameters`:

* amount (Amount): product price. Available payment methods can change depending on this parameter;
* title (String): product name;
* subtitle (String): product description;
* clientApplicationKey (String): store's token received in YooMoney;
* shopId (String): store's ID in YooMoney;
* paymentId (String): payment ID.
* savePaymentMethod (SavePaymentMethod): configuration for saving payment methods. Saved payment methods can be used for processing recurring payments.

Fields of the `Amount` class:
* value (BigDecimal): amount;
* currency (Currency): currency.

Values of `SavePaymentMethod`:
* ON: Save the payment methods for processing recurring payments. Only payment methods which support saving will be available to users. A notification that the payment method will be saved will be displayed on the contract screen.
* OFF: Don't save the payment method.
* USER_SELECTS: User selects if the payment method should be used or not. If a method can be used, a switch will appear on the contract screen.

```java
class MyActivity extends AppCompatActivity {

    ...

    void timeToStartCheckout() {
        SavedBankCardPaymentParameters parameters = new SavedBankCardPaymentParameters(
                new Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
                "Product name",
                "Product description",
                "live_AAAAAAAAAAAAAAAAAAAA",
                "12345",
                "paymentId",
                SavePaymentMethod.OFF
        );
        Intent intent = Checkout.createSavedCardTokenizeIntent(this, parameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
}
```

### Getting tokenization results
Tokenization result will be returned in `onActivityResult()`.

Possible result types:
* Activity.RESULT_OK: tokenization completed successfully;
* Activity.RESULT_CANCELED: user has canceled tokenization;

In case of a successful tokenization, the SDK will return a token and the type of the payment tool using which it was received.
Use the `Checkout.createTokenizationResult()` method to receive the token.

`Checkout.createTokenizationResult()` accepts `Intent` received in `onActivityResult()` in case of a successful tokenization as input. It returns TokenizationResult which consists of:
* paymentToken (String): payment token, see [Using the payment token](#using-the-payment-token);
* paymentMethodType (PaymentMethodType): type of the payment method.

Values of `PaymentMethodType`:
* YOO_MONEY: the payment was made via the YooMoney wallet;
* BANK_CARD: the payment was made via a bank card;
* SBERBANK: the payment was made via Sberbank (text message invoicing or SberPay);
* GOOGLE_PAY: the payment was made via Google Pay.

```java
public final class MainActivity extends AppCompatActivity {

    ...

     @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE_TOKENIZE) {
                switch (resultCode) {
                    case RESULT_OK:
                        // successful tokenization
                        TokenizationResult result = Checkout.createTokenizationResult(data);
                        ...
                        break;
                    case RESULT_CANCELED:
                        // user canceled tokenization
                        ...
                        break;
                }
            }
        }
}
```

### Using the payment token
You need to ask your YooMoney manager for permission to process payments using the token.
A token can only be used once, it's valid for 1 hour. If you haven't created a payment within one hour, you'll need to request a new token.

The payment token contains information about the [payment confirmation scenario](https://yookassa.ru/developers/payments/payment-process#user-confirmation).
Once you receive the payment token, you'll be able to [create a payment](https://yookassa.ru/developers/api#create_payment), enter the payment token in the `payment_token` parameter.
If the payment is processed with authentication via 3-D Secure, use `confirmation_url` receive in the [Payment](https://yookassa.ru/developers/api#payment_object) object.
Use `confirmation_url` to run 3-D Secure, see [3DSecure](#3DSecure).

You can also get [information about the payment](https://yookassa.ru/developers/api#get_payment)

### Test parameters and debugging

You can add the `TestParameters` object when calling `Checkout.createTokenizeIntent()` to debug tokenization.

Fields of the `TestParameters:` class
* showLogs (Boolean): display SDK logs. All logs start with the 'YooKassa.SDK' tag
* googlePayTestEnvironment (Boolean): use Google Pay's test environment: all transactions processed via Google Pay will use `WalletConstants.ENVIRONMENT_TEST`. Please note that if you try paying with googlePayTestEnvironment=true, you'll get a tokenization error. To learn more, see https://developers.google.com/pay/api/android/guides/test-and-deploy/integration-checklist#about-the-test-environment.
* mockConfiguration (MockConfiguration): use a mock configuration. If this parameter is present, the SDK will work in offline mode and generate a test token. You won't be able to use this token for payments.

`MockConfiguration`
The library has a test mode where you can see how SDK works with different input data.
This mode doesn't require Internet access. Received token can't be used for payments.

Fields of the `MockConfiguration` class:
* completeWithError (Boolean): tokenization always returns an error;
* paymentAuthPassed (Boolean): user is always authorized;
* linkedCardsCount (Int): number of cards linked to user's wallet;
* serviceFee (Amount): commission displayed in the contract;

```java
class MyActivity extends AppCompatActivity {

    ...

    void timeToStartCheckout() {
        PaymentParameters paymentParameters = new PaymentParameters(...);
        TestParameters testParameters = new TestParameters(true, true,
            new MockConfiguration(false, true, 5, new Amount(BigDecimal.TEN, Currency.getInstance("RUB"))));
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters, testParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
}
```

### Interface configuration

You can use the`UiParameters` object for configuring the SDK interface. You can configure interface colors and if the YooMoney logo is displayed or hidden.

Fields of the `UiParameters` class:
* showLogo (Boolean): show/hide the YooMoney logo on the screen with payment methods.
* colorScheme (ColorScheme): color scheme.

Fields of the `ColorScheme` class:
* primaryColor (ColorInt): main color of the app. This color will be used for buttons, switches, input fields, etc.
We don't recommend choosing very bright colors (they won't be seen with a white backgroung) or red (it'll interfere with the color of error messages).

```java
class MyActivity extends AppCompatActivity {

    ...

    void timeToStartCheckout() {
        PaymentParameters paymentParameters = new PaymentParameters(...);
        UiParameters uiParameters = new UiParameters(true, new ColorScheme(Color.rgb(0, 114, 245)));
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters, new TestParameters(), uiParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
}
```

## 3DSecure

The SDK contains an Activity for processing 3DS to simplify the integration of payments via bank cards.
Don't specify PaymentParameters.customReturnUrl when calling Checkout.createTokenizeIntent(), if you use this Activity.

Input parameters for `Checkout.createConfirmationIntent()`:
* context (Context): context for creating `Intent`;
* url (String): URL for redirect to 3DS.
* paymentMethodType (PaymentMethodType): selected payment method type.
* colorScheme (ColorScheme): color scheme.

You can receive 3ds results in `onActivityResult()`

Possible result types:
* Activity.RESULT_OK: notifies that the 3ds process is finished but doesn't guarantee that it was successful. We recommend requesting the payment status after receiving the result;
* Activity.RESULT_CANCELED: 3ds process has been canceled (for example, if the user tapped the "back" button during the process);
* Checkout.RESULT_ERROR: couldn't complete 3ds.

**Launching 3ds and getting results**
```java
class MyActivity extends AppCompatActivity {

    void timeToStart3DS() {
        Intent intent = Checkout.createConfirmationIntent(
                this,
                "https://3dsurl.com/",
                PaymentMethodType.BANK_CARD
        );
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    // 3ds process finished
                    break;
                case RESULT_CANCELED:
                    // 3ds screen has been closed
                    break;
                case Checkout.RESULT_ERROR:
                    // an error occurred during 3ds (for example, no connection)
                    // you can learn more in data
                    // data.getIntExtra(Checkout.EXTRA_ERROR_CODE): error code from WebViewClient.ERROR_* or Checkout.ERROR_NOT_HTTPS_URL
                    // data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION): error description (it can be empty)
                    // data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL): url where the error occurred (it can be empty)
                    break;
            }
        }
    }
}
```

## Scanning bank cards

Create an `Activity` which processes action `ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD`

***Implementing the activity for scanning***
```xml
<activity android:name=".ScanBankCardActivity">

    <intent-filter>
        <action android:name="ru.yoomoney.sdk.kassa.payments.action.SCAN_BANK_CARD"/>
    </intent-filter>

</activity>
```

Launch your library for scanning cards in this `Activity`.
Enter the card number you received using `Intent` as shown in the example below.
Don't forget to specify `Activity.RESULT_OK` if the card was scanned successfully.

***Returning the result with activity***
```java
public class ScanBankCardActivity extends Activity {

    private void onScanningDone(final String cardNumber, final int expirationMonth, final int expirationYear) {

        final Intent result = Checkout.createScanBankCardResult(cardNumber, expirationMonth, expirationYear);

        setResult(Activity.RESULT_OK, result);

        finish();

    }

}
```

# Useful links
* [YooMoney website](https://yookassa.ru/en)
* [Documentation for mobile SDKs on the YooMoney website](https://yookassa.ru/docs/client-sdks/#mobil-nye-sdk)
* [Demo app in Google Play](https://play.google.com/store/apps/details?id=ru.yoo.sdk.kassa.payments.example.release)
* [SDK for iOS](https://github.com/yoomoney/yookassa-payments-swift)