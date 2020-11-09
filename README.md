# Android Checkout mobile SDK

Эта библиотека позволяет встроить прием платежей в мобильные приложения на Android.
Она работает как дополнение к API Яндекс.Кассы.

В SDK входят готовые платежные интерфейсы (форма оплаты и всё, что с ней связано).

С помощью SDK можно получать токены для проведения оплаты с банковской карты, Google Pay, Сбербанка или из кошелька в Яндекс.Деньгах.

В этом репозитории лежит код SDK и пример приложения, которое его интегрирует.
* [Код библиотеки](https://github.com/yandex-money/yandex-checkout-android-sdk/tree/master/library)
* [Код демо-приложения, которое интегрирует SDK](https://github.com/yandex-money/yandex-checkout-android-sdk/tree/master/sample)


#  Документация

Android Checkout mobile SDK - версия 4.1.0 ([changelog](https://github.com/yandex-money/yandex-checkout-android-sdk/blob/master/CHANGELOG.md))

* [Changelog](#changelog)
* [Migration guide](#migration-guide)
* [Подключение зависимостей](#подключение-зависимостей)
    * [Подключение через Gradle](#подключение-через-Gradle)
    * [Подключение ru.yoo.sdk.auth (для платежей из кошелька)](#подключение-ru.yoo.sdk.auth)
    * [Настройка приложения при продаже цифровых товаров](#настройка-приложения-при-продаже-цифровых-товаров)
* [Использование библиотеки](#использование-библиотеки)
    * [Токенизация](#токенизация)
        * [Запуск токенизации](#запуск-токенизации)
        * [Запуск токенизации для сохранённых банковских карт](#запуск-токенизации-для-сохранённых-банковских-карт)
        * [Получение результата токенизации](#получение-результата-токенизации)
        * [Использование платежного токена](#использование-платежного-токена)
        * [Тестовые параметры и отладка](#тестовые-параметры-и-отладка)
        * [Настройка интерфейса](#настройка-интерфейса)
    * [3DSecure](#3dsecure)
    * [Сканирование банковской карты](#сканирование-банковской-карты)
* [Полезные ссылки](#полезные-ссылки)

# Changelog

[Ссылка на Changelog](https://github.com/yandex-money/yandex-checkout-android-sdk/blob/master/CHANGELOG.md)

# Migration guide

[Ссылка на Migration guide](https://github.com/yandex-money/yandex-checkout-android-sdk/blob/master/MIGRATION.md)

# Подключение зависимостей

## Подключение через Gradle
Для подключения библиотеки пропишите зависимости в build.gradle модуля:

```groovy
repositories {
    maven { url 'https://dl.bintray.com/yandex-money/maven' }
}

dependencies {
    implementation 'com.yandex.money:checkout:4.1.0'
}
```

Попросите у менеджера по подключению библиотеку `ThreatMetrix Android SDK 5.4-73.aar`. 
Создайте папку libs в модуле где подключаете sdk и добавьте туда файл `ThreatMetrix Android SDK 5.4-73.aar`. В build.gradle того же модуля в dependencies добавьте:
```groovy
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
}
```

## Подключение ru.yoo.sdk.auth (для платежей из кошелька)
Если среди платёжных методов есть кошелёк Яндекс.Денег, необходимо подключить `ru.yoo.sdk.auth`.
В остальных случаях этот шаг можно пропустить.

Попросить менеджера по подключению зарегистрировать для вас приложение в центре авторизации.

```groovy
repositories {
    maven { url 'https://dl.bintray.com/yoomoney/maven' }
}
dependencies {
    implementation "ru.yoo.sdk.auth:auth:1.0.29"
}
```

Попросите у менеджера по подключению библиотеку `ui-lib-1.19.5.aar`.
Создайте папку `libs` в модуле где подключаете sdk и положите туда файл `ui-lib-1.19.5.aar`. В `build.gradle` того же модуля в dependencies добавьте:
```groovy
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
}
```

## Настройка приложения при продаже цифровых товаров
Если в вашем приложении продаются цифровые товары, нужно отключить Google Pay из списка платежных опций.
Для этого добавьте в AndroidManifest следующий код:

```xml
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    tools:node="remove" />
```

# Использование библиотеки

Вся работа с библиотекой происходит через обращения к классу `ru.yandex.money.android.sdk.Checkout`

## Токенизация

### Запуск токенизации

Для запуска процесса токенизации используется метод `Checkout.createTokenizeIntent()`. Метод отдаёт `Intent`, который нужно запустить в startActivityForResult(). После этого управление процессом перейдёт в SDK.
Готовый платёжный токен можно получить в `onActivityResult()` (см. [Получение результата токенизации](#получение-результата-токенизации))

Входные параметры метода:
* context (Context) - контекст приложения;
* paymentParameters (PaymentParameters) - параметры платежа;
* testParameters (TestParameters) - параметры для дебага, см. [Тестовые параметры и отладка](#тестовые-параметры-и-отладка);
* uiParameters (UiParameters) - настройка интерфейса, см. [Настройка интерфейса](#настройка-интерфейса).

Поля `PaymentParameters`:

Обязательные:
* amount (Amount) - стоимость товара. Допустимые способы оплаты могут меняться в зависимости от этого параметра;
* title (String) - название товара;
* subtitle (String) - описание товара;
* clientApplicationKey (String) - ключ для клиентских приложений из личного кабинета Яндекс.Кассы ([раздел Настройки — Ключи API](https://kassa.yandex.ru/my/api-keys-settings)).;
* shopId (String) - идентификатор магазина в Яндекс.Кассе.
* savePaymentMethod (SavePaymentMethod) - настройка сохранения платёжного метода. Сохранённые платёжные методы можно использовать для проведения рекуррентных платежей.
* clientId (String) - идентификатор приложения для sdk авторизации `ru.yoo.sdk.auth`, нужно запросить у менеджера по подключению.

Необязательные:
* paymentMethodTypes (Set of PaymentMethodType) - ограничения способов оплаты. Если оставить поле пустым или передать в него null,
библиотека будет использовать все доступные способы оплаты;
* gatewayId (String) - gatewayId для магазина;
* customReturnUrl (String) - url страницы (поддерживается только https), на которую надо вернуться после прохождения 3ds. Должен использоваться только при при использовании своего Activity для 3ds url. При использовании Checkout.create3dsIntent() не задавайте этот параметр;
* userPhoneNumber (String) - номер телефона пользователя. Используется для автозаполнения поля при оплате через Сбербанк Онлайн. Поддерживаемый формат данных: "+7XXXXXXXXXX".
* googlePayParameters (GooglePayParameters) - настройки для оплаты через Google Pay.

Поля класса `Amount`:
* value (BigDecimal) - сумма;
* currency (Currency) - валюта.

Значения `SavePaymentMethod`:
* ON - Сохранить платёжный метод для проведения рекуррентных платежей. Пользователю будут доступны только способы оплаты, поддерживающие сохранение. На экране контракта будет отображено сообщение о том, что платёжный метод будет сохранён.
* OFF - Не сохранять платёжный метод.
* USER_SELECTS - Пользователь выбирает, сохранять платёжный метод или нет. Если метод можно сохранить, на экране контракта появится переключатель.

Значения `PaymentMethodType`:
* YANDEX_MONEY - оплата произведена с кошелька Яндекс.денег;
* BANK_CARD - оплата произведена с банковской карты;
* SBERBANK - оплата произведена через Сбербанк (SMS invoicing или Сбербанк онлайн);
* GOOGLE_PAY - оплата произведена через Google Pay.

Поля класса `GooglePayParameters`:
* allowedCardNetworks (Set of GooglePayCardNetwork) - платежные системы, через которые возможна оплата с помощью Google Pay.

Значения `GooglePayCardNetwork`:
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
                "Название товара",
                "Описание товара",
                "live_AAAAAAAAAAAAAAAAAAAA",
                "12345",
                SavePaymentMethod.OFF
        );
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
}
```

### Запуск токенизации для сохранённых банковских карт

Данный способ токенизации используется в случае, если есть привязанная к магазину карта и необходимо заново запросить у пользователя её csc.
В остальных случаях следует использовать стандартный механизм токенизации (см. [Запуск токенизации](#запуск-токенизации)).

Для запуска процесса токенизации с платежным идентификатором используется метод `Checkout.createSavedCardTokenizeIntent()`. Метод отдаёт `Intent`, который нужно запустить в startActivityForResult().
Готовый платёжный токен можно получить в `onActivityResult()` (см. [Получение результата токенизации](#получение-результата-токенизации))

Входные параметры метода:
* context (Context) - контекст приложения;
* savedBankCardPaymentParameters (SavedBankCardPaymentParameters) - параметры платежа с сохранённой банковской картой;
* testParameters (TestParameters) - параметры для дебага, см. [Тестовые параметры и отладка](#тестовые-параметры-и-отладка);
* uiParameters (UiParameters) - настройка интерфейса, см. [Настройка интерфейса](#настройка-интерфейса).

Поля `SavedBankCardPaymentParameters`:

* amount (Amount) - стоимость товара. Допустимые способы оплаты могут меняться в зависимости от этого параметра;
* title (String) - название товара;
* subtitle (String) - описание товара;
* clientApplicationKey (String) - токен магазина, полученный в Яндекс.Кассе;
* shopId (String) - идентификатор магазина в Яндекс.Кассе;
* paymentId (String) - идентификатор платежа.
* savePaymentMethod (SavePaymentMethod) - настройка сохранения платёжного метода. Сохранённые платёжные методы можно использовать для проведения рекуррентных платежей.

Поля класса `Amount`:
* value (BigDecimal) - сумма;
* currency (Currency) - валюта.

Значения `SavePaymentMethod`:
* ON - Сохранить платёжный метод для проведения рекуррентных платежей. Пользователю будут доступны только способы оплаты, поддерживающие сохранение. На экране контракта будет отображено сообщение о том, что платёжный метод будет сохранён.
* OFF - Не сохранять платёжный метод.
* USER_SELECTS - Пользователь выбирает, сохранять платёжный метод или нет. Если метод можно сохранить, на экране контракта появится переключатель.

```java
class MyActivity extends AppCompatActivity {

    ...

    void timeToStartCheckout() {
        SavedBankCardPaymentParameters parameters = new SavedBankCardPaymentParameters(
                new Amount(BigDecimal.TEN, Currency.getInstance("RUB")),
                "Название товара",
                "Описание товара",
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

### Получение результата токенизации
Результат токенизации будет возвращен в `onActivityResult()`.

Возможные типы результата:
* Activity.RESULT_OK - токенизация прошла успешно;
* Activity.RESULT_CANCELED - пользователь отменил токенизацию;

В случае успешной токенизации SDK вернёт токен и тип платежного инструмента, с помощью которого он был получен.
Для получения токена используйте метод `Checkout.createTokenizationResult()`.

`Checkout.createTokenizationResult()` принимает на вход `Intent`, полученный в `onActivityResult()` при успешной токенизации. Он возвращает TokenizationResult, который состоит из:
* paymentToken (String) - платежный токен, см. [Использование платежного токена](#использование-платежного-токена);
* paymentMethodType (PaymentMethodType) - тип платежного средства.

Значения `PaymentMethodType`:
* YANDEX_MONEY - оплата произведена с кошелька Яндекс.денег;
* BANK_CARD - оплата произведена с банковской карты;
* SBERBANK - оплата произведена через Сбербанк (SMS invoicing или Сбербанк онлайн);
* GOOGLE_PAY - оплата произведена через Google Pay.

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

### Использование платежного токена
Необходимо получить у менеджера Яндекс.Кассы разрешение на проведение платежей с использованием токена. 
Токен одноразовый, срок действия — 1 час. Если не создать платеж в течение часа, токен нужно будет запрашивать заново.

В платежном токене содержатся данные о [сценарии подтверждения](https://kassa.yandex.ru/developers/payments/payment-process#user-confirmation) платежа.
После получения платежного токена Вы можете [создать платеж](https://kassa.yandex.ru/developers/api#create_payment), в параметре `payment_token` передайте платежный токен.
Если платеж проводится с аутентификацией по 3-D Secure, используйте `confirmation_url`, который придет в объекте [Платежа](https://kassa.yandex.ru/developers/api#payment_object). 
Используйте `confirmation_url` для запуска 3-D Secure, см. [3DSecure](#3DSecure).

Так же, Вы можете получить [информацию о платеже](https://kassa.yandex.ru/developers/api#get_payment) 

### Тестовые параметры и отладка

Для отладки токенизации в вызов `Checkout.createTokenizeIntent()` можно добавить объект `TestParameters`.

Поля класса `TestParameters:`
* showLogs (Boolean) - включить отображение логов SDK. Все логи начинаются с тега 'Yandex.Checkout.SDK'
* googlePayTestEnvironment (Boolean) - использовать тестовую среду Google Pay - все транзакции, проведенные через Google Pay, будут использовать `WalletConstants.ENVIRONMENT_TEST`. Имейте ввиду, что при попытке оплаты с параметром googlePayTestEnvironment=true произойдет ошибка токенизации. Подробнее см. на https://developers.google.com/pay/api/android/guides/test-and-deploy/integration-checklist#about-the-test-environment. 
* mockConfiguration (MockConfiguration) - использовать моковую конфигурацию. Если этот параметр присутствует, SDK будет работать в оффлайн режиме и генерировать тестовый токен. Этот токен нельзя использовать для платежей.

`MockConfiguration`
В библиотеке есть тестовый режим, с помощью которого можно посмотреть, как будет выглядеть работа SDK при различных входных данных.
Для работы этого режима не нужен доступ в интернет. Полученный токен нельзя использовать для оплаты.

Поля класса `MockConfiguration`:
* completeWithError (Boolean) - токенизация всегда возвращает ошибку;
* paymentAuthPassed (Boolean) - пользователь всегда авторизован;
* linkedCardsCount (Int) - количество карт, привязанных к кошельку пользователя;
* serviceFee (Amount) - комиссия, которая будет отображена на контракте;

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

### Настройка интерфейса

Для настройки интерфейса SDK можно использовать объект `UiParameters`. Можно настроить цвета интерфейса и показ/скрытие логотипа Яндекс.Кассы.

Поля класса `UiParameters`:
* showLogo (Boolean) - показать/скрыть лого Яндекс.Кассы на экране способов оплаты.
* colorScheme (ColorScheme) - цветовая схема.

Поля класса `ColorScheme`:
* primaryColor (ColorInt) - основной цвет приложения. В этот цвет будут краситься кнопки, переключатели, поля для ввода и т.д. 
Не рекомендуется задавать в качестве этого цвета слишком светлые цвета (они будут не видны на белом фоне) и красный цвет (он будет пересекаться с цветом ошибки).

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

Для упрощения интеграции платежей по банковским картам в SDK есть Activity для обработки 3DS.
Не указывайте PaymentParameters.customReturnUrl при вызове Checkout.createTokenizeIntent(), если используете это Activity.

Входные параметры для `Checkout.create3dsIntent()`:
* context (Context) - контекст для создания `Intent`;
* url (String) - URL для перехода на 3DS.
* colorScheme (ColorScheme) - цветовая схема.

Результат работы 3ds можно получить в `onActivityResult()`

Возможные типы результата:
* Activity.RESULT_OK - 3ds прошёл успешно;
* Activity.RESULT_CANCELED - прохождение 3ds было отменено (например, пользователь нажал на кнопку "назад" во время процесса);
* Checkout.RESULT_ERROR - не удалось пройти 3ds.

**Запуск 3ds и получение результата**
```java
class MyActivity extends AppCompatActivity {
    
    void timeToStart3DS() {
        Intent intent = Checkout.create3dsIntent(
                this,
                "https://3dsurl.com/"
        );
        startActivityForResult(intent, 1);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    // 3ds прошел
                    break;
                case RESULT_CANCELED:
                    // экран 3ds был закрыт
                    break;
                case Checkout.RESULT_ERROR:
                    // во время 3ds произошла какая-то ошибка (нет соединения или что-то еще)
                    // более подробную информацию можно посмотреть в data
                    // data.getIntExtra(Checkout.EXTRA_ERROR_CODE) - код ошибки из WebViewClient.ERROR_* или Checkout.ERROR_NOT_HTTPS_URL
                    // data.getStringExtra(Checkout.EXTRA_ERROR_DESCRIPTION) - описание ошибки (может отсутствовать)
                    // data.getStringExtra(Checkout.EXTRA_ERROR_FAILING_URL) - url по которому произошла ошибка (может отсутствовать)
                    break;
            }
        }
    }
}
```

## Сканирование банковской карты

Создайте `Activity`, обрабатывающую action `ru.yandex.money.android.sdk.action.SCAN_BANK_CARD`

***Подключение activity для сканирования***
```xml
<activity android:name=".ScanBankCardActivity">

    <intent-filter>
        <action android:name="ru.yandex.money.android.sdk.action.SCAN_BANK_CARD"/>
    </intent-filter>

</activity>
```

В этой `Activity` запустите Вашу библиотеку для сканирования карты.
Полученный номер карты передайте c помощью `Intent`, как показано в примере ниже.
Не забудьте поставить `Activity.RESULT_OK`, если сканирование прошло успешно.

***Возвращение результата с activity***
```java
public class ScanBankCardActivity extends Activity {
    
    private void onScanningDone(final String cardNumber, final int expirationMonth, final int expirationYear) {
    
        final Intent result = Checkout.createScanBankCardResult(cardNumber, expirationMonth, expirationYear);
    
        setResult(Activity.RESULT_OK, result);
    
        finish();
    
    }
    
}
```

# Полезные ссылки
* [Сайт Яндекс.Кассы](https://kassa.yandex.ru)
* [Документация мобильных SDK на сайте Яндекс.Кассы](https://kassa.yandex.ru/docs/client-sdks/#mobil-nye-sdk)
* [Демо-приложение в Google Play](https://play.google.com/store/apps/details?id=ru.yandex.money.android.example.prod)
* [SDK для iOS](https://github.com/yandex-money/yandex-checkout-payments-swift)