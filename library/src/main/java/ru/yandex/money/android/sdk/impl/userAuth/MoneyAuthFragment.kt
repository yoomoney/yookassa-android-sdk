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

package ru.yandex.money.android.sdk.impl.userAuth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.yoo.sdk.auth.account.model.UserAccount
import ru.yoo.sdk.auth.Config
import ru.yoo.sdk.auth.ThemeScheme
import ru.yoo.sdk.auth.YooMoneyAuth
import ru.yoo.sdk.auth.YooMoneyAuth.KEY_ACCESS_TOKEN
import ru.yoo.sdk.auth.YooMoneyAuth.REQUEST_MONEY_AUTHORIZATION
import ru.yandex.money.android.sdk.BuildConfig.AUTH_HOST
import ru.yandex.money.android.sdk.BuildConfig.YANDEX_CLIENT_ID
import ru.yandex.money.android.sdk.R
import ru.yandex.money.android.sdk.impl.AppModel
import ru.yandex.money.android.sdk.impl.InMemoryColorSchemeRepository
import ru.yandex.money.android.sdk.impl.UserAgent
import ru.yoo.sdk.auth.RemoteConfig
import ru.yoo.sdk.auth.analytics.AnalyticsEvent
import ru.yoo.sdk.auth.analytics.AnalyticsLogger

internal class YandexAuthFragment : Fragment(), YandexAuthorizeUserGateway.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModel.yandexAuthGateway?.setListener(this)
    }

    override fun onDestroy() {
        AppModel.yandexAuthGateway?.removeListener()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MONEY_AUTHORIZATION) {
            val token = data?.getStringExtra(KEY_ACCESS_TOKEN)
            val userAccount = data?.getParcelableExtra<UserAccount>(YooMoneyAuth.KEY_USER_ACCOUNT)
            val tmxProcessId = data?.getStringExtra(YooMoneyAuth.KEY_TMX_SESSION_ID)
            if (token != null) {
                AppModel.yandexAuthGateway?.setResult(token, userAccount)
                AppModel.tmxSessionIdStorage.tmxSessionId = tmxProcessId
            } else {
                AppModel.yandexAuthGateway?.cancel()
            }
        }
    }

    override fun collectCurrentUser(authCenterClientId: String) {
        YooMoneyAuth.initAnalyticsLogger(object: AnalyticsLogger {
            override fun onNewEvent(event: AnalyticsEvent) {
                AppModel.reporter.report(event.toString())
            }
        })
        val config = Config(
            origin = Config.Origin.WALLET,
            processType = Config.ProcessType.LOGIN,
            themeScheme = ThemeScheme.byAccentColor(InMemoryColorSchemeRepository.colorScheme.primaryColor),
            authCenterClientId = authCenterClientId,
            yandexClientId = YANDEX_CLIENT_ID,
            debugApiHost = if (AUTH_HOST.isNotEmpty()) AUTH_HOST else null,
            supportEmail = getString(R.string.ym_support_email),
            supportHelpUrl = getString(R.string.ym_support_help_url),
            supportPhone = getString(R.string.ym_support_phone),
            migrationBannerVisible = true,
            migrationBannerText = getString(R.string.ym_migration_banner_text),
            migrationBannerButtonText = getString(R.string.ym_migration_banner_button_text),
            migrationBannerImageUrl = "https://static.yoomoney.ru/files-front/mobile/img/android_migration_banner_logo.png",
            applicationUserAgent = UserAgent.getUserAgent(requireContext()),
            remoteConfig = RemoteConfig(
                restorePasswordEnabled = false,
                userAgreementTitle = getString(R.string.auth_remote_config_offer_no_email),
                userWithEmailAgreementTitle = getString(R.string.auth_remote_config_offer_has_email),
                emailCheckboxVisible = true,
                migrationScreenTitle = getString(R.string.auth_soft_migration_title),
                migrationScreenSubtitle = getString(R.string.auth_soft_migration_subtitle),
                migrationScreenButtonSubtitle = getString(R.string.auth_soft_migration_action_subtitle),
                hardMigrationScreenTitle = getString(R.string.auth_hard_migration_title),
                hardMigrationScreenSubtitle = getString(R.string.auth_hard_migration_subtitle),
                hardMigrationScreenButtonSubtitle = getString(R.string.auth_hard_migration_action_subtitle)
            )
        )
        startActivityForResult(YooMoneyAuth.auth(requireContext(), config), REQUEST_MONEY_AUTHORIZATION)
    }
}
