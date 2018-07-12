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

package ru.yandex.money.android.sdk.impl

import android.annotation.SuppressLint
import android.content.Context
import ru.yandex.money.android.sdk.AuthorizedUser
import ru.yandex.money.android.sdk.Configuration
import ru.yandex.money.android.sdk.CurrentUser
import ru.yandex.money.android.sdk.ShopParameters
import ru.yandex.money.android.sdk.StateHolder
import ru.yandex.money.android.sdk.impl.contract.ContractErrorPresenter
import ru.yandex.money.android.sdk.impl.contract.ContractPresenter
import ru.yandex.money.android.sdk.impl.contract.SelectPaymentOptionController
import ru.yandex.money.android.sdk.impl.logging.MsdkLogger
import ru.yandex.money.android.sdk.impl.logging.ReporterLogger
import ru.yandex.money.android.sdk.impl.logout.LogoutController
import ru.yandex.money.android.sdk.impl.logout.LogoutGatewayImpl
import ru.yandex.money.android.sdk.impl.logout.LogoutSuccessViewModel
import ru.yandex.money.android.sdk.impl.metrics.ActionChangePaymentMethodReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionLogoutReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionPaymentAuthorizationErrorReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionPaymentAuthorizationReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionTokenizeReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionYaLoginAuthorizationFailedReporter
import ru.yandex.money.android.sdk.impl.metrics.ActionYaLoginAuthorizationReporter
import ru.yandex.money.android.sdk.impl.metrics.ContractOpenedReporter
import ru.yandex.money.android.sdk.impl.metrics.ErrorReportingLogger
import ru.yandex.money.android.sdk.impl.metrics.ErrorScreenOpenedReporter
import ru.yandex.money.android.sdk.impl.metrics.ExceptionReportingLogger
import ru.yandex.money.android.sdk.impl.metrics.LinkedCardScreenOpenedReporter
import ru.yandex.money.android.sdk.impl.metrics.NewBankCardScreenOpenedReporter
import ru.yandex.money.android.sdk.impl.metrics.PaymentOptionListOpenedReporter
import ru.yandex.money.android.sdk.impl.metrics.Reporter
import ru.yandex.money.android.sdk.impl.metrics.ResetTokenizeSchemeWrapper
import ru.yandex.money.android.sdk.impl.metrics.SelectPaymentOptionTokenizeSchemeSetter
import ru.yandex.money.android.sdk.impl.metrics.SessionReporter
import ru.yandex.money.android.sdk.impl.metrics.TokenizeSchemeParamProvider
import ru.yandex.money.android.sdk.impl.metrics.UserAuthTokenTypeParamProvider
import ru.yandex.money.android.sdk.impl.metrics.UserAuthTypeParamProvider
import ru.yandex.money.android.sdk.impl.metrics.YandexMetricaErrorReporter
import ru.yandex.money.android.sdk.impl.metrics.YandexMetricaExceptionReporter
import ru.yandex.money.android.sdk.impl.metrics.YandexMetricaReporter
import ru.yandex.money.android.sdk.impl.metrics.YandexMetricaSessionReporter
import ru.yandex.money.android.sdk.impl.payment.PaymentErrorPresenter
import ru.yandex.money.android.sdk.impl.payment.SharedPreferencesCurrentUserGateway
import ru.yandex.money.android.sdk.impl.payment.tokenize.ApiV3TokenizeGateway
import ru.yandex.money.android.sdk.impl.payment.tokenize.MockTokenizeGateway
import ru.yandex.money.android.sdk.impl.payment.tokenize.TokenizeController
import ru.yandex.money.android.sdk.impl.payment.tokenize.TokenizeErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.ApiV3PaymentAuthGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockPaymentAuthTypeGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockProcessPaymentAuthGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockSmsSessionRetryGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.ProcessPaymentAuthController
import ru.yandex.money.android.sdk.impl.paymentAuth.ProcessPaymentAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.RequestPaymentAuthController
import ru.yandex.money.android.sdk.impl.paymentAuth.RequestPaymentAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.SelectAppropriateAuthType
import ru.yandex.money.android.sdk.impl.paymentAuth.SmsSessionRetryController
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.PaymentOptionInfoPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.ApiV3PaymentOptionListGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.ChangePaymentOptionController
import ru.yandex.money.android.sdk.impl.paymentOptionList.GooglePayIntegration
import ru.yandex.money.android.sdk.impl.paymentOptionList.InternetDependentGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.LoadPaymentOptionListController
import ru.yandex.money.android.sdk.impl.paymentOptionList.MockPaymentOptionListGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListProgressPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.StubCheckGooglePayAvailableGateway
import ru.yandex.money.android.sdk.impl.secure.BcKeyStorage
import ru.yandex.money.android.sdk.impl.secure.Decrypter
import ru.yandex.money.android.sdk.impl.secure.Encrypter
import ru.yandex.money.android.sdk.impl.secure.SharedPreferencesIvStorage
import ru.yandex.money.android.sdk.impl.secure.getPlatformPassword
import ru.yandex.money.android.sdk.impl.userAuth.MockAuthorizeUserGateway
import ru.yandex.money.android.sdk.impl.userAuth.MockWalletCheckGateway
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthController
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthPresenter
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthorizeUserGateway
import ru.yandex.money.android.sdk.impl.userAuth.YandexWalletCheckGateway
import ru.yandex.money.android.sdk.logout.LogoutUseCase
import ru.yandex.money.android.sdk.newHttpClient
import ru.yandex.money.android.sdk.payment.CheckPaymentAuthRequiredGateway
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.GetLoadedPaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.InMemoryPaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.SaveLoadedPaymentOptionsListGateway
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionUseCase
import ru.yandex.money.android.sdk.payment.loadOptionList.CheckGooglePayAvailableGateway
import ru.yandex.money.android.sdk.payment.loadOptionList.LoadPaymentOptionListUseCase
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListWithGooglePayFilterGateway
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionUseCase
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeGateway
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeUseCase
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTokenGateway
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTypeGateway
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthGateway
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthUseCase
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthUseCase
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryGateway
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryUseCase
import ru.yandex.money.android.sdk.userAuth.AuthorizeUserGateway
import ru.yandex.money.android.sdk.userAuth.UserAuthTokenGateway
import ru.yandex.money.android.sdk.userAuth.UserAuthUseCase
import ru.yandex.money.android.sdk.userAuth.WalletCheckGateway

private const val USER_STORAGE_TEST_MODE = "userStorageTestMode"
private const val USER_STORAGE_REAL_MODE = "userStorageRealMode"

@SuppressLint("StaticFieldLeak")
internal object AppModel {

    internal lateinit var sessionReporter: SessionReporter
    private lateinit var shopParameters: ShopParameters
    private lateinit var tokensStorage: TokensStorage

    val listeners = StateHolder(mainExecutor)

    var yandexAuthGateway: YandexAuthorizeUserGateway? = null
        private set
    var googlePayIntegration: GooglePayIntegration? = null
        private set

    lateinit var loadPaymentOptionListController: LoadPaymentOptionListController
        private set
    lateinit var changePaymentOptionController: ChangePaymentOptionController
        private set
    lateinit var selectPaymentOptionController: SelectPaymentOptionController
        private set
    lateinit var tokenizeController: TokenizeController
        private set
    lateinit var userAuthController: UserAuthController
        private set
    lateinit var logoutController: LogoutController
        private set
    lateinit var requestPaymentAuthController: RequestPaymentAuthController
        private set
    lateinit var processPaymentAuthController: ProcessPaymentAuthController
        private set
    lateinit var smsSessionRetryController: SmsSessionRetryController
        private set

    fun init(
        argContext: Context,
        shopParameters: ShopParameters,
        configuration: Configuration?,
        requestRecurringPayment: Boolean = false
    ) {

        val context = argContext.applicationContext
        this.shopParameters = shopParameters

        val preferencesStorage = if (configuration == null || !configuration.enableTestMode)
            USER_STORAGE_REAL_MODE else USER_STORAGE_TEST_MODE
        val sharedPreferences = context.getSharedPreferences(preferencesStorage, Context.MODE_PRIVATE)

        val keyStorage: Lazy<BcKeyStorage> = lazy {
            BcKeyStorage(context, "bc.keystore", getPlatformPassword(context))
        }
        val ivStorage = SharedPreferencesIvStorage(sharedPreferences)
        val keyKey = "cipherKey"
        val ivKey = "cipherIv"
        val encrypt = Encrypter(
            getKey = { keyStorage.value.getOrCreate(keyKey) },
            getIv = { ivStorage.getOrCreate(ivKey) }
        )
        val decrypt = Decrypter(
            getKey = { checkNotNull(keyStorage.value.get(keyKey)) },
            getIv = { checkNotNull(ivStorage.get(ivKey)) }
        )
        tokensStorage = TokensStorage(
            preferences = sharedPreferences,
            encrypt = encrypt,
            decrypt = decrypt
        )

        val currentUserGateway: CurrentUserGateway
        val paymentOptionListGateway: PaymentOptionListGateway
        val getLoadedPaymentOptionListGateway: GetLoadedPaymentOptionListGateway = InMemoryPaymentOptionListGateway
        val saveLoadedPaymentOptionsListGateway: SaveLoadedPaymentOptionsListGateway = InMemoryPaymentOptionListGateway
        val tokenizeGateway: TokenizeGateway
        val authorizeUserGateway: AuthorizeUserGateway
        val paymentAuthTypeGateway: PaymentAuthTypeGateway
        val processPaymentAuthGateway: ProcessPaymentAuthGateway
        val smsSessionRetryGateway: SmsSessionRetryGateway
        val walletCheckGateway: WalletCheckGateway
        val userAuthTokenGateway: UserAuthTokenGateway
        val paymentAuthTokenGateway: PaymentAuthTokenGateway
        val checkPaymentAuthRequiredGateway: CheckPaymentAuthRequiredGateway
        val checkGooglePayAvailableGateway: CheckGooglePayAvailableGateway

        googlePayIntegration = if (shopParameters.enableGooglePay) {
            GooglePayIntegration(
                context = context,
                shopId = requireNotNull(shopParameters.shopId) {
                    "ShopId can't be null if Google Pay is present in payment method types"
                },
                useTestEnvironment = configuration?.googlePayTestEnvironment == true,
                loadedPaymentOptionsGateway = getLoadedPaymentOptionListGateway
            )
        } else {
            null
        }

        if (configuration?.enableTestMode == true) {
            val mockPaymentOptionListGateway = MockPaymentOptionListGateway(configuration.linkedCardsCount)
            paymentOptionListGateway = mockPaymentOptionListGateway
            authorizeUserGateway = MockAuthorizeUserGateway
            tokenizeGateway = MockTokenizeGateway(configuration.completeWithError)
            paymentAuthTypeGateway = MockPaymentAuthTypeGateway()
            processPaymentAuthGateway = MockProcessPaymentAuthGateway()
            smsSessionRetryGateway = MockSmsSessionRetryGateway()
            walletCheckGateway = MockWalletCheckGateway()
            if (configuration.paymentAuthPassed) {
                currentUserGateway = object : CurrentUserGateway {
                    override var currentUser: CurrentUser = AuthorizedUser("testUser")
                }
                userAuthTokenGateway = object : UserAuthTokenGateway {
                    override var userAuthToken: String? = "userAuthToken"
                }
                paymentAuthTokenGateway = object : PaymentAuthTokenGateway {
                    override var paymentAuthToken: String? = "paymentAuthToken"
                    override val isPaymentAuthPersisted: Boolean = true
                    override fun persistPaymentAuth() {
                        // does nothing
                    }
                }
                checkPaymentAuthRequiredGateway = object : CheckPaymentAuthRequiredGateway {
                    override fun checkPaymentAuthRequired() = false
                }
            } else {
                currentUserGateway = SharedPreferencesCurrentUserGateway(sharedPreferences)
                userAuthTokenGateway = tokensStorage
                paymentAuthTokenGateway = tokensStorage
                checkPaymentAuthRequiredGateway = tokensStorage
            }
            checkGooglePayAvailableGateway = StubCheckGooglePayAvailableGateway(configuration.googlePayAvailable)
        } else {
            currentUserGateway = SharedPreferencesCurrentUserGateway(sharedPreferences)
            val httpClient = newHttpClient(context)
            val apiV3PaymentOptionListGateway = ApiV3PaymentOptionListGateway(
                httpClient = httpClient,
                gatewayId = shopParameters.gatewayId,
                tokensStorage = tokensStorage,
                shopToken = shopParameters.clientApplicationKey
            )
            paymentOptionListGateway = apiV3PaymentOptionListGateway
            authorizeUserGateway = YandexAuthorizeUserGateway(mainExecutor, context)
            yandexAuthGateway = authorizeUserGateway
            val profilingTool = ThreatMetrixProfilingTool()
            profilingTool.init(context)
            tokenizeGateway = ApiV3TokenizeGateway(
                httpClient = httpClient,
                shopToken = shopParameters.clientApplicationKey,
                paymentAuthTokenGateway = tokensStorage,
                tmxProfilingTool = profilingTool
            )
            val paymentAuthGateway = ApiV3PaymentAuthGateway(
                httpClient = httpClient,
                tokensStorage = tokensStorage,
                shopToken = shopParameters.clientApplicationKey,
                tmxProfilingTool = profilingTool,
                selectAppropriateAuthType = SelectAppropriateAuthType()
            )
            paymentAuthTypeGateway = paymentAuthGateway
            processPaymentAuthGateway = paymentAuthGateway
            smsSessionRetryGateway = paymentAuthGateway
            walletCheckGateway = YandexWalletCheckGateway(httpClient)
            userAuthTokenGateway = tokensStorage
            paymentAuthTokenGateway = tokensStorage
            checkPaymentAuthRequiredGateway = tokensStorage
            checkGooglePayAvailableGateway = if (shopParameters.enableGooglePay) {
                googlePayIntegration as CheckGooglePayAvailableGateway
            } else {
                StubCheckGooglePayAvailableGateway(shopParameters.enableGooglePay)
            }
        }

        val gateway = InternetDependentGateway(
            context = context,
            paymentOptionListGateway = PaymentOptionListWithGooglePayFilterGateway(
                paymentOptionListGateway = paymentOptionListGateway,
                googlePayAvailableGateway = checkGooglePayAvailableGateway
            )
        )
        val paymentOptionListUseCase = LoadPaymentOptionListUseCase(
            paymentOptionListRestrictions = shopParameters.paymentMethodTypes,
            paymentOptionListGateway = gateway,
            saveLoadedPaymentOptionsListGateway = saveLoadedPaymentOptionsListGateway,
            currentUserGateway = currentUserGateway
        )

        val reporter: Reporter = ReporterLogger(YandexMetricaReporter(context))
        val errorReporter = YandexMetricaErrorReporter(context)
        val exceptionReporter = YandexMetricaExceptionReporter(context)
        sessionReporter = YandexMetricaSessionReporter(context)

        val userAuthTypeParamProvider = UserAuthTypeParamProvider(currentUserGateway, checkPaymentAuthRequiredGateway)
        val userAuthTokenTypeParamProvider = UserAuthTokenTypeParamProvider(paymentAuthTokenGateway)
        val tokenizeSchemeParamProvider = TokenizeSchemeParamProvider()
        val paymentOptionListPresenter = PaymentOptionListPresenter(context, shopParameters.showLogo)
        val paymentOptionListProgressPresenter = PaymentOptionListProgressPresenter(shopParameters.showLogo)
        val errorPresenter = ErrorPresenter(context)
        val paymentErrorPresenter = PaymentErrorPresenter(errorPresenter)
        val paymentOptionListErrorPresenter = PaymentOptionListErrorPresenter(
            context = context,
            showLogo = shopParameters.showLogo,
            errorPresenter = paymentErrorPresenter
        )
        val contractPresenter = ContractPresenter(
            context = context,
            shopTitle = shopParameters.title,
            shopSubtitle = shopParameters.subtitle,
            recurringPaymentsPossible = requestRecurringPayment
        )
        val contractErrorPresenter = ContractErrorPresenter(errorPresenter)
        val requestPaymentAuthErrorPresenter = RequestPaymentAuthErrorPresenter(context, errorPresenter)

        val logger = ExceptionReportingLogger(
            logger = ErrorReportingLogger(
                logger = MsdkLogger(),
                errorReporter = errorReporter
            ),
            exceptionReporter = exceptionReporter
        )

        loadPaymentOptionListController = LoadPaymentOptionListController(
            paymentOptionListUseCase = ResetTokenizeSchemeWrapper(
                useCase = paymentOptionListUseCase,
                setTokenizeScheme = tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            paymentOptionListPresenter = PaymentOptionListOpenedReporter(
                getUserAuthType = userAuthTypeParamProvider,
                presenter = paymentOptionListPresenter,
                reporter = reporter
            ),
            progressPresenter = paymentOptionListProgressPresenter,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = paymentOptionListErrorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        changePaymentOptionController = ChangePaymentOptionController(
            paymentOptionListUseCase = ResetTokenizeSchemeWrapper(
                useCase = ChangePaymentOptionUseCase(getLoadedPaymentOptionListGateway),
                setTokenizeScheme = tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            paymentOptionListPresenter = ActionChangePaymentMethodReporter(
                presenter = paymentOptionListPresenter,
                reporter = reporter
            ),
            progressPresenter = paymentOptionListProgressPresenter,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = paymentOptionListErrorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        selectPaymentOptionController = SelectPaymentOptionController(
            selectPaymentOptionUseCase = SelectPaymentOptionTokenizeSchemeSetter(
                SelectPaymentOptionUseCase(
                    getLoadedPaymentOptionListGateway = getLoadedPaymentOptionListGateway,
                    checkPaymentAuthRequiredGateway = checkPaymentAuthRequiredGateway
                ),
                tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            contractPresenter = ContractOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = {
                    checkNotNull(tokenizeSchemeParamProvider()) { "TokenizeScheme should be present" }
                },
                presenter = contractPresenter::invoke,
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = contractErrorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        tokenizeController = TokenizeController(
            tokenizeUseCase = TokenizeUseCase(
                getLoadedPaymentOptionListGateway = getLoadedPaymentOptionListGateway,
                tokenizeGateway = tokenizeGateway,
                checkPaymentAuthRequiredGateway = checkPaymentAuthRequiredGateway
            ),
            successPresenter = ActionTokenizeReporter(
                getUserAuthType = userAuthTypeParamProvider,
                getUserAuthTokenType = userAuthTokenTypeParamProvider,
                presenter = NewBankCardScreenOpenedReporter(
                    getAuthType = userAuthTypeParamProvider,
                    presenter = LinkedCardScreenOpenedReporter(
                        presenter = PaymentOptionInfoPresenter(contractPresenter::invoke),
                        reporter = reporter
                    ),
                    reporter = reporter
                ),
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = TokenizeErrorPresenter(paymentErrorPresenter),
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        userAuthController = UserAuthController(
            userAuthUseCase = UserAuthUseCase(
                authorizeUserGateway = authorizeUserGateway,
                currentUserGateway = currentUserGateway,
                userAuthTokenGateway = userAuthTokenGateway,
                walletCheckGateway = walletCheckGateway
            ),
            userAuthPresenter = ActionYaLoginAuthorizationReporter(
                presenter = UserAuthPresenter(),
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = ActionYaLoginAuthorizationFailedReporter(
                    presenter = UserAuthErrorPresenter(errorPresenter),
                    reporter = reporter
                ),
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        logoutController = LogoutController(
            logoutUseCase = LogoutUseCase(
                LogoutGatewayImpl(
                    currentUserGateway = currentUserGateway,
                    userAuthTokenGateway = userAuthTokenGateway,
                    paymentAuthTokenGateway = paymentAuthTokenGateway,
                    removeKeys = {
                        ivStorage.remove(ivKey)
                        keyStorage.value.remove(keyKey)
                        encrypt.reset()
                        decrypt.reset()
                    }
                )
            ),
            logoutPresenter = ActionLogoutReporter({ LogoutSuccessViewModel() }, reporter),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = errorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        requestPaymentAuthController = RequestPaymentAuthController(
            requestPaymentAuthUseCase = RequestPaymentAuthUseCase(paymentAuthTypeGateway),
            progressPresenter = contractPresenter::invoke,
            requestPaymentAuthPresenter = contractPresenter::invoke,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = requestPaymentAuthErrorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        processPaymentAuthController = ProcessPaymentAuthController(
            processPaymentUseCase = ProcessPaymentAuthUseCase(
                processPaymentAuthGateway = processPaymentAuthGateway,
                currentUserGateway = currentUserGateway,
                paymentAuthTokenGateway = paymentAuthTokenGateway
            ),
            progressPresenter = contractPresenter::invoke,
            processPaymentAuthPresenter = ActionPaymentAuthorizationReporter(
                presenter = contractPresenter::invoke,
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = ActionPaymentAuthorizationErrorReporter(
                    presenter = ProcessPaymentAuthErrorPresenter(context, errorPresenter),
                    reporter = reporter
                ),
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        smsSessionRetryController = SmsSessionRetryController(
            smsSessionRetryUseCase = SmsSessionRetryUseCase(smsSessionRetryGateway),
            progressPresenter = contractPresenter::invoke,
            smsSessionRetryPresenter = contractPresenter::invoke,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = requestPaymentAuthErrorPresenter,
                reporter = reporter
            ),
            resultConsumer = listeners::onEvent,
            logger = logger
        )
    }

    fun reset() {
        listeners.reset()
        loadPaymentOptionListController.reset()
        selectPaymentOptionController.reset()
        tokenizeController.reset()
        userAuthController.reset()
        requestPaymentAuthController.reset()
        processPaymentAuthController.reset()

        yandexAuthGateway?.reset()
        googlePayIntegration?.reset()
        tokensStorage.reset()
    }
}
