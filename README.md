# Android Checkout MSDK

Эта библиотека позволяет встроить прием платежей в мобильные приложения на iOS и Android,
работает как дополнение к API Яндекс.Кассы. В mSDK входят готовые платежные интерфейсы (форма оплаты и всё, что с ней связано).

С помощью mSDK можно получать токены для проведения оплаты с банковской карты, из кошелька
в Яндекс.Деньгах или с карты, привязанной к кошельку.

# Подключение библиотеки

Для подключения библиотеки нужно прописать зависимости в файле build.gradle в вашем проекте:

```groovy
repositories {
    maven { url 'https://dl.bintray.com/yandex-money/maven' }
}
dependencies {
    implementation 'com.yandex.money:checkout:1.3.0'
}
```

Вся работа с библиотекой происходит через обращения к классу `ru.yandex.money.android.sdk.Checkout`

# Авторизация в Яндексе (для платежей из кошелька)
Можно не подключать, если планируется использовать оплату только новыми банковскими картами: 
```
new ShopParameters("...", "...", "...", Collections.singleton(PaymentMethodType.BANK_CARD))
```

На странице [создания приложения](https://oauth.yandex.ru/client/new), в разделе *Платформы* надо выбрать *Android приложение*

В разделе *API Яндекс.Паспорта* надо добавить *Доступ к логину, имени, фамилии и полу* для корректного отображения имени пользователя

```groovy
android {
    defaultConfig {
        manifestPlaceholders = [YANDEX_CLIENT_ID:"<идентификатор вашего приложения>"]
    }
}
repositories {
    mavenCentral()
}
dependencies {
    implementation 'com.yandex.android:authsdk:2.1.0'
}
```

# Подключение библиотеки к фрагменту/активити
Для подключения интерфейса нужно вызвать метод `Checkout.attach()` до начала токенизации
(желательно вызвать его сразу после создания фрагмента/активити, из которого будет произведен запуск)
Во время уничтожения фрагмента/активити нужно вызвать `Checkout.detach()`

Входные параметры метода Checkout.attach():
* supportFragmentManager (FragmentManager) - менеджер фрагментов.


**Пример**
```java
public final class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Checkout.attach(getSupportFragmentManager());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Checkout.detach();
    }    
}
```

# Запуск токенизации

Для запуска процесса токенизации нужно вызвать метод `Checkout.tokenize()`. После этого управление процессом перейдёт в MSDK.

Входные параметры метода:
* context (Context) - контекст приложения;
* amount (Amount) - сумма платежа;
* shopParameters (ShopParameters) - параметры магазина, которые не меняются для разных платежей.

Поля класса ShopParameters:
* title (String) - название магазина;
* subtitle (String) - описание магазина;
* clientApplicationKey (String) - токен магазина, полученный в Яндекс.Кассе;
* paymentMethodTypes (PaymentMethodType) - ограничения способов оплаты. Если оставить поле пустым или передать в него null,
библиотека будет использовать все доступные способы оплаты;
* enableGooglePay (Boolean) - включить/выключить Google Pay (при включении надо задать поле shopId и пройти процесс подключения Google Pay в Яндекс.Кассе);
* gatewayId (String) - идентификатор магазина в Яндекс.Кассе в который пойдет платеж (не обязательно);
* shopId (String) - идентификатор магазина в Яндекс.Кассе (нужен при платеже Google Pay);
* showLogo (Boolean) - показать/скрыть лого Яндекс.Кассы.

Поля класса Amount:
* value (BigDecimal) - сумма;
* currency (Currency) - валюта.

Значения PaymentMethodType:
* YANDEX_MONEY - оплата произведена с кошелька Яндекс.денег;
* BANK_CARD - оплата произведена с банковской карты;
* SBERBANK - оплата произведена через Сбербанк (SMS invoicing или Сбербанк онлайн);
* GOOGLE_PAY - оплата произведена через Google Pay.

**Пример:**
```java
class MyActivity extends android.support.v7.app.AppCompatActivity {
    
    //other code
    
    void timeToStartCheckout() {
        Checkout.tokenize(
             this,
             new Amount(new BigDecimal("10.0"), Currency.getInstance("RUB")),
             new ShopParameters(
                   "Название магазина",
                   "Описание магазина",
                   "Токен, полученный в Яндекс.кассе"));
    }
}
```

# Получение результата токенизации

Для получения результата токенизации используется метод `Checkout.setResultCallback()`
В случае успешной токенизации MSDK вернёт токен и платежный инструмент, с помощью которого он был получен.

Входные параметры метода:
* resultCallback (Checkout.ResultCallback) - колбэк, который будет вызван после успешной токенизации.

Checkout.ResultCallback возвращает:
* paymentToken (String) - платежный токен;
* paymentMethodType(PaymentMethodType) - тип платежного средства.


Значения PaymentMethodType:
* YANDEX_MONEY - оплата произведена с кошелька Яндекс.денег;
* BANK_CARD - оплата произведена с банковской карты.


**Пример:**
```java
class MyActivity extends android.support.v7.app.AppCompatActivity {
    
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        Checkout.setResultCallback(new Checkout.ResultCallback() {
            @Override
            public void onResult(@NonNull String paymentToken, @NonNull PaymentMethodType type) {
                //result handling
            }
        });
    }
}
```

# 3DSecure

Для упрощения интеграции в MSDK есть Activity для обработки 3DS.

Входные параметры для `Checkout.create3dsIntent()`:
* context (Context) - контекст для создания `Intent`
* url (URL) - URL для перехода на 3DS
* redirectUrl (URL) - URL для возврата с 3DS

**Пример:**
```java
class MyActivity extends android.support.v7.app.AppCompatActivity {
    
    void timeToStart3DS() {
        Intent intent = Checkout.create3dsIntent(
                this,
                URL("https://3dsurl.com/"),
                URL("https://3dsend.org/")
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

# Сканирование банковской карты

Создайте `Activity`, обрабатывающую action `ru.yandex.money.android.sdk.action.SCAN_BANK_CARD`

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
```java
public class ScanBankCardActivity extends Activity {
    
    private void onScanningDone(final String cardNumber, final int expirationMonth, final int expirationYear) {
    
        final Intent result = Checkout.createScanBankCardResult(cardNumber, expirationMonth, expirationYear);
    
        setResult(Activity.RESULT_OK, result);
    
        finish();
    
    }
    
}
```

# Тестовый режим

В библиотеке предусмотрен тестовый режим, позволяющий эмулировать действия SDK без использования реальных источников данных.
Для запуска тестового режима необходимо вызвать метод `Checkout.configureTestMode()` и передать ему объект `Configuration`.
**Это надо сделать до вызова `Checkout.tokenize()`.**

***Пример***
```java
class MyActivity extends android.support.v7.app.AppCompatActivity {

    //other code

    void timeToStartCheckout() {

        Checkout.configureTestMode(
            new Configuration(
                // enableTestMode - is test mode enabled,
                // completeWithError - complete tokenization with error,
                // paymentAuthPassed - user authenticated,
                // linkedCardsCount - test linked cards count,
                // googlePayAvailable - emulate google pay availability,
                // googlePayTestEnvironment - use google pay test environment
            )
        );

        Checkout.tokenize(
             this,
             new Amount(new BigDecimal("10.0"), Currency.getInstance("RUB")),
             new ShopParameters(
                 "Название магазина",
                 "Описание магазина",
                 "Токен, полученный в Яндекс.кассе"
            )
        );
    }
}
```

# Полезные ссылки
* [Подключение Яндекс.Кассы](https://kassa.yandex.ru)
* [Документация МСДК](https://kassa.yandex.ru/docs/client-sdks/#mobil-nye-sdk)
* [Демо-приложение в Google Play](https://play.google.com/store/apps/details?id=ru.yandex.money.android.example.prod)
