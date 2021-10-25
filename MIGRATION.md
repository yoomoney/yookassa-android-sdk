# Migration guide

### Table of Contents
[6.4.0 -> 6.4.1](##6---6)
- Обновить файл библитеки ThreatMetrix-Android-SDK.aar новой версией 6.2.97.
  Новую версию нужно попросить прислать вашего менеджера по подключению YooKassa.

[6.0.0 -> 6.0.2](##6---6)
- Убрать зависимость `implementation ("ru.yoomoney.sdk.auth:auth:$yoo_sdk_auth_version")` - теперь она подтягивается автоматически

[5.1.4 -> 6.0.0](##5---6)
- [Заменить вызов метода Checkout.create3dsIntent на Checkout.createConfirmationIntent](###Заменить-вызов-метода-Checkout.create3dsIntent-на-Checkout.createConfirmationIntent)
- [Добавить схему вашего приложения, для этого добавить строку ym_app_scheme со значением вашей схемы в strings.xml.]
Нужно добавить в ваш файл build.gradle в блок android.defaultConfig строку `resValue "string", "ym_app_scheme", "your_unique_app_shceme"`
```
android {
    defaultConfig {
        resValue "string", "ym_app_scheme", "your_unique_app_shceme"
    }
}
```
Или добавить в ваш strings.xml строку вида:
```
<resources>
    <string name="ym_app_scheme" translatable="false">your_unique_app_shceme</string>
</resources>
```

[4.1.0 -> 5.1.0](##4---5)
[5.0.3 -> 5.1.0](##5---5)
- [Удалить библиотеку `ui-lib.aar` из папки libs, она больше не нужна](###Удалить-библиотеку-`ui-lib.aar`-из папки-libs,-она-больше-не-нужна)

[4.1.0 -> 5.0.1](##4---5)
- [Обновить версию `ui-lib-1.20.2.aar`](###Обновить-версию-`ui-lib-1.20.2.a ar`)
- [Заменить имена пакетов](###заменить-имена-пакетов)

[4.0.1 -> 4.1.0](##4---4)
- [Обновить версию `ui-lib-1.19.5.aar`](###Обновить-версию-`ui-lib-1.19.5.aar`)

[3.\*.\* -> 4.0.0](##3---4)
- [Подключить ru.yoomoney.sdk.auth](###Подключить-sdk-авторизации)
- [Подключить ThreatMetrix Android SDK 5.4-73.aar](###Подключить-ThreatMetrix-Android-SDK-`5.4-73.aar`)
- [Подключить ui-lib-1.19.4.aar](###Подключить-`ui-lib-1.19.4.aar`)

## 4.1.0 -> 5.0.1
### Обновить версию `ui-lib-1.20.2.aar`
> Если вы не использовали платежный метод “ЮMoney”, и не подключали sdk авторизации, то этот блок можно пропустить.

Попросить у менеджера по подключению новую библиотеку `ui-lib-1.20.2.aar` и положить её в папку `libs`. Старую версию `ui-lib-1.19.5.aar` нужно удалить.

### Заменить имена пакетов
Нужно заменить старые названия пакетов библиотеки на новые `ru.yoomoney.sdk.kassa.payments.*`

## 4.0.1 -> 4.1.0

### Обновить версию `ui-lib-1.19.5.aar`

> Если вы не использовали платежный метод “ЮMoney”, и не подключали sdk авторизации, то этот блок можно пропустить.

Попросить у менеджера по подключению новую библиотеку `ui-lib-1.19.5.aar` и положить её в папку `libs`. Старую версию `ui-lib-1.19.4.aar` нужно удалить.

## 3.\*.\* -> 4.0.0

### Подключить sdk авторизации

> Если вы не использовали платежный метод “ЮMoney”, и не подключали sdk авторизации, то этот блок можно пропустить.

Попросить менеджера по подключению зарегистрировать для вас приложение в центре авторизации.

Прописать в `build.gradle`

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation "ru.yoomoney.sdk.auth:auth:$yoo_sdk_auth_version"
}
```

### **Подключить `ThreatMetrix Android SDK 5.4-73.aar`**

> Если вы не использовали платежный метод “ЮMoney”, и не подключали sdk авторизации, то этот блок можно пропустить.

Попросите у менеджера по подключению библиотеку `ThreatMetrix Android SDK 5.4-73.aar`. Создайте папку libs в модуле где подключаете sdk и добавьте туда файл `ThreatMetrix Android SDK 5.4-73.aar`. В build.gradle того же модуля в dependencies добавьте:

```
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
}

```

### **Подключить `ui-lib-1.19.4.aar`**

> Если вы не использовали платежный метод “ЮMoney”, и не подключали sdk авторизации, то этот блок можно пропустить.

Создайте папку `libs` в модуле где подключаете sdk и положите туда файл `ui-lib-1.19.5.aar`. В `build.gradle` того же модуля в dependencies добавьте:

```
dependencies {
    implementation fileTree(dir: "libs", include: ["*.aar"])
}
```
