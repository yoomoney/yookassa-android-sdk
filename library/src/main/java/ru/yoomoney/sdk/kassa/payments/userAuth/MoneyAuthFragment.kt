/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.userAuth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.yoomoney.sdk.auth.Config
import ru.yoomoney.sdk.auth.RemoteConfig
import ru.yoomoney.sdk.auth.YooMoneyAuth
import ru.yoomoney.sdk.auth.YooMoneyAuth.KEY_ACCESS_TOKEN
import ru.yoomoney.sdk.auth.YooMoneyAuth.REQUEST_MONEY_AUTHORIZATION
import ru.yoomoney.sdk.auth.analytics.AnalyticsEvent
import ru.yoomoney.sdk.auth.analytics.AnalyticsLogger
import ru.yoomoney.sdk.auth.auxAuthorization.model.AuxTokenScope
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.di.CheckoutInjector
import ru.yoomoney.sdk.kassa.payments.http.UserAgent
import ru.yoomoney.sdk.kassa.payments.metrics.Reporter
import ru.yoomoney.sdk.kassa.payments.navigation.Router
import ru.yoomoney.sdk.kassa.payments.navigation.Screen
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuth.Action.Authorized
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuth.Action.RequireAuth
import ru.yoomoney.sdk.kassa.payments.userAuth.di.UserAuthModule
import ru.yoomoney.sdk.kassa.payments.utils.viewModel
import ru.yoomoney.sdk.march.RuntimeViewModel
import ru.yoomoney.sdk.march.observe
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.http.HostProvider
import ru.yoomoney.sdk.kassa.payments.metrics.MoneyAuthLoginSchemeAuthSdk
import ru.yoomoney.sdk.kassa.payments.utils.canResolveIntent
import javax.inject.Inject

private const val REQUEST_CODE_APP_2_APP_AUTHORIZATION = 317

internal typealias MoneyAuthViewModel = RuntimeViewModel<MoneyAuth.State, MoneyAuth.Action, Unit>

internal class MoneyAuthFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var reporter: Reporter

    @Inject
    lateinit var paymentParameters: PaymentParameters

    @Inject
    lateinit var testParameters: TestParameters

    @Inject
    lateinit var hostProvider: HostProvider

    private val yooMoneyIntent by lazy {
        createYooMoneyAuxDeepLink(
            requireNotNull(paymentParameters.authCenterClientId),
            listOf(
                AuxTokenScope.Wallet.Balance,
                AuxTokenScope.UserAuthCenter.AccountInfo
            )
        )
    }

    private val isYooMoneyCouldBeOpened: Boolean
        get() = requireActivity().canResolveIntent(yooMoneyIntent)

    private val viewModel: MoneyAuthViewModel by viewModel(UserAuthModule.MONEY_AUTH) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CheckoutInjector.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ym_fragment_money_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observe(
            lifecycleOwner = viewLifecycleOwner,
            onState = ::showState,
            onEffect = {},
            onFail = {}
        )
    }

    fun authorize() {
        viewModel.handleAction(RequireAuth(isYooMoneyCouldBeOpened))
    }

    private fun showState(state: MoneyAuth.State) {
        when (state) {
            is MoneyAuth.State.Authorize ->
                when (state.authorizeStrategy) {
                    is MoneyAuth.AuthorizeStrategy.InApp -> collectCurrentUser()
                    is MoneyAuth.AuthorizeStrategy.App2App -> collectCurrentUserFromYooMoneyApp()
                }
            MoneyAuth.State.CompleteAuth -> router.navigateTo(
                Screen.PaymentOptions(
                    Screen.MoneyAuth.Result.SUCCESS
                )
            )
            MoneyAuth.State.CancelAuth -> router.navigateTo(
                Screen.PaymentOptions(
                    Screen.MoneyAuth.Result.CANCEL
                )
            )
            MoneyAuth.State.WaitingForAuthStarted -> Unit
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_MONEY_AUTHORIZATION -> {
                if (data?.getStringExtra(KEY_ACCESS_TOKEN) == null) {
                    router.navigateTo(
                        Screen.PaymentOptions(
                            Screen.MoneyAuth.Result.CANCEL
                        )
                    )
                    return
                }

                viewModel.handleAction(
                    Authorized(
                        token = data.getStringExtra(KEY_ACCESS_TOKEN),
                        userAccount = data.getParcelableExtra(YooMoneyAuth.KEY_USER_ACCOUNT),
                        tmxSessionId = data.getStringExtra(YooMoneyAuth.KEY_TMX_SESSION_ID),
                        typeAuth = MoneyAuthLoginSchemeAuthSdk()
                    )
                )
            }
            REQUEST_CODE_APP_2_APP_AUTHORIZATION -> {
                val cryptogram = data?.getStringExtra(YooMoneyAuth.KEY_CRYPTOGRAM)
                if (cryptogram == null) {
                    router.navigateTo(Screen.PaymentOptions(Screen.MoneyAuth.Result.CANCEL))
                    return
                }
                viewModel.handleAction(MoneyAuth.Action.GetTransferData(cryptogram))
            }
            else -> Unit
        }
    }

    private fun collectCurrentUser() {
        YooMoneyAuth.initAnalyticsLogger(object : AnalyticsLogger {
            override fun onNewEvent(event: AnalyticsEvent) {
                reporter.report(event.toString())
            }
        })
        val config = Config(
            origin = Config.Origin.WALLET,
            processType = Config.ProcessType.LOGIN,
            authCenterClientId = requireNotNull(paymentParameters.authCenterClientId),
            yandexClientId = testParameters.yandexClientId,
            apiHost = if (!hostProvider.authHost().isNullOrEmpty()) hostProvider.authHost() else null,
            isDebugMode = !hostProvider.authHost().isNullOrEmpty(),
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

    private fun collectCurrentUserFromYooMoneyApp() {
        startActivityForResult(yooMoneyIntent, REQUEST_CODE_APP_2_APP_AUTHORIZATION)
    }
}
