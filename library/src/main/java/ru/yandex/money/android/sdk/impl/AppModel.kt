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
import com.google.android.gms.security.ProviderInstaller
import ru.yandex.money.android.sdk.PaymentMethodType
import ru.yandex.money.android.sdk.PaymentParameters
import ru.yandex.money.android.sdk.TestParameters
import ru.yandex.money.android.sdk.UiParameters
import ru.yandex.money.android.sdk.impl.InMemoryColorSchemeRepository.colorScheme
import ru.yandex.money.android.sdk.impl.contract.ContractErrorPresenter
import ru.yandex.money.android.sdk.impl.contract.ContractFormatter
import ru.yandex.money.android.sdk.impl.contract.ContractPresenter
import ru.yandex.money.android.sdk.impl.contract.ContractProgressViewModel
import ru.yandex.money.android.sdk.impl.contract.ContractViewModel
import ru.yandex.money.android.sdk.impl.extensions.getConfirmation
import ru.yandex.money.android.sdk.impl.logging.MsdkLogger
import ru.yandex.money.android.sdk.impl.logging.ReporterLogger
import ru.yandex.money.android.sdk.impl.logging.StubLogger
import ru.yandex.money.android.sdk.impl.logout.LogoutFailViewModel
import ru.yandex.money.android.sdk.impl.logout.LogoutGatewayImpl
import ru.yandex.money.android.sdk.impl.logout.LogoutProgressViewModel
import ru.yandex.money.android.sdk.impl.logout.LogoutSuccessViewModel
import ru.yandex.money.android.sdk.impl.logout.LogoutViewModel
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
import ru.yandex.money.android.sdk.impl.metrics.RecurringCardScreenOpenedReporter
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
import ru.yandex.money.android.sdk.impl.payment.tokenize.TokenizeErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.ApiV3PaymentAuthGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockPaymentAuthTypeGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockProcessPaymentAuthGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.MockSmsSessionRetryGateway
import ru.yandex.money.android.sdk.impl.paymentAuth.ProcessPaymentAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.ProcessPaymentAuthProgressViewModel
import ru.yandex.money.android.sdk.impl.paymentAuth.RequestPaymentAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentAuth.RequestPaymentAuthProgressViewModel
import ru.yandex.money.android.sdk.impl.paymentAuth.SelectAppropriateAuthType
import ru.yandex.money.android.sdk.impl.paymentAuth.SmsSessionRetryProgressViewModel
import ru.yandex.money.android.sdk.impl.paymentMethodInfo.ApiV3PaymentMethodInfoGateway
import ru.yandex.money.android.sdk.impl.paymentMethodInfo.MockPaymentInfoGateway
import ru.yandex.money.android.sdk.impl.paymentOptionInfo.PaymentOptionInfoPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.ApiV3PaymentOptionListGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.ChangePaymentOptionPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.GooglePayIntegration
import ru.yandex.money.android.sdk.impl.paymentOptionList.InternetDependentGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.MockPaymentOptionListGateway
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListErrorPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListProgressPresenter
import ru.yandex.money.android.sdk.impl.paymentOptionList.PaymentOptionListViewModel
import ru.yandex.money.android.sdk.impl.paymentOptionList.StubCheckGooglePayAvailableGateway
import ru.yandex.money.android.sdk.impl.secure.BcKeyStorage
import ru.yandex.money.android.sdk.impl.secure.Decrypter
import ru.yandex.money.android.sdk.impl.secure.Encrypter
import ru.yandex.money.android.sdk.impl.secure.SharedPreferencesIvStorage
import ru.yandex.money.android.sdk.impl.secure.getPlatformPassword
import ru.yandex.money.android.sdk.impl.userAuth.MockAuthorizeUserGateway
import ru.yandex.money.android.sdk.impl.userAuth.MockWalletCheckGateway
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthErrorPresenter
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthPresenter
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthProgressViewModel
import ru.yandex.money.android.sdk.impl.userAuth.UserAuthViewModel
import ru.yandex.money.android.sdk.impl.userAuth.YandexAuthorizeUserGateway
import ru.yandex.money.android.sdk.impl.userAuth.YandexWalletCheckGateway
import ru.yandex.money.android.sdk.logout.LogoutInputModel
import ru.yandex.money.android.sdk.logout.LogoutOutputModel
import ru.yandex.money.android.sdk.logout.LogoutUseCase
import ru.yandex.money.android.sdk.model.AuthorizedUser
import ru.yandex.money.android.sdk.model.Controller
import ru.yandex.money.android.sdk.model.CurrentUser
import ru.yandex.money.android.sdk.model.Fee
import ru.yandex.money.android.sdk.model.SdkException
import ru.yandex.money.android.sdk.model.StateHolder
import ru.yandex.money.android.sdk.model.ViewModel
import ru.yandex.money.android.sdk.model.newHttpClient
import ru.yandex.money.android.sdk.payment.CheckPaymentAuthRequiredGateway
import ru.yandex.money.android.sdk.payment.CurrentUserGateway
import ru.yandex.money.android.sdk.payment.GetLoadedPaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.InMemoryPaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.SaveLoadedPaymentOptionsListGateway
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionInputModel
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionOutputModel
import ru.yandex.money.android.sdk.payment.changeOption.ChangePaymentOptionUseCase
import ru.yandex.money.android.sdk.payment.loadOptionList.CheckGooglePayAvailableGateway
import ru.yandex.money.android.sdk.payment.loadOptionList.LoadPaymentOptionListUseCase
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListGateway
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListInputModel
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListOutputModel
import ru.yandex.money.android.sdk.payment.loadOptionList.PaymentOptionListWithGooglePayFilterGateway
import ru.yandex.money.android.sdk.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionInputModel
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionOutputModel
import ru.yandex.money.android.sdk.payment.selectOption.SelectPaymentOptionUseCase
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeGateway
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeInputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeOutputModel
import ru.yandex.money.android.sdk.payment.tokenize.TokenizeUseCase
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTokenGateway
import ru.yandex.money.android.sdk.paymentAuth.PaymentAuthTypeGateway
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthGateway
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthInputModel
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthOutputModel
import ru.yandex.money.android.sdk.paymentAuth.ProcessPaymentAuthUseCase
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthInputModel
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthOutputModel
import ru.yandex.money.android.sdk.paymentAuth.RequestPaymentAuthUseCase
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryGateway
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryInputModel
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryOutputModel
import ru.yandex.money.android.sdk.paymentAuth.SmsSessionRetryUseCase
import ru.yandex.money.android.sdk.userAuth.AuthorizeUserGateway
import ru.yandex.money.android.sdk.userAuth.UserAuthInputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthOutputModel
import ru.yandex.money.android.sdk.userAuth.UserAuthTokenGateway
import ru.yandex.money.android.sdk.userAuth.UserAuthUseCase
import ru.yandex.money.android.sdk.userAuth.WalletCheckGateway
import ru.yoo.sdk.auth.YooMoneyAuth

private const val USER_STORAGE_TEST_MODE = "userStorageTestMode"
private const val USER_STORAGE_REAL_MODE = "userStorageRealMode"

// Do not forget to change redirect link in test.html at /assets
internal const val DEFAULT_REDIRECT_URL = "checkoutsdk://success"

@SuppressLint("StaticFieldLeak")
internal object AppModel {

    internal lateinit var sessionReporter: SessionReporter
    private lateinit var tokensStorage: TokensStorage

    val listeners = StateHolder(mainExecutor)

    val tmxSessionIdStorage = TmxSessionIdStorage()

    var yandexAuthGateway: YandexAuthorizeUserGateway? = null
        private set
    var googlePayIntegration: GooglePayIntegration? = null
        private set

    var userPhoneNumber: String? = null

    lateinit var loadPaymentOptionListController:
            Controller<PaymentOptionListInputModel, PaymentOptionListOutputModel, PaymentOptionListViewModel>
        private set
    lateinit var changePaymentOptionController:
            Controller<ChangePaymentOptionInputModel, ChangePaymentOptionOutputModel, PaymentOptionListViewModel>
        private set
    lateinit var selectPaymentOptionController:
            Controller<SelectPaymentOptionInputModel, SelectPaymentOptionOutputModel, ContractViewModel>
        private set
    lateinit var tokenizeController: Controller<TokenizeInputModel, TokenizeOutputModel, ViewModel>
        private set
    lateinit var userAuthController: Controller<UserAuthInputModel, UserAuthOutputModel, UserAuthViewModel>
        private set
    lateinit var logoutController: Controller<LogoutInputModel, LogoutOutputModel, LogoutViewModel>
        private set
    lateinit var requestPaymentAuthController:
            Controller<RequestPaymentAuthInputModel, RequestPaymentAuthOutputModel, ContractViewModel>
        private set
    lateinit var processPaymentAuthController:
            Controller<ProcessPaymentAuthInputModel, ProcessPaymentAuthOutputModel, ContractViewModel>
        private set

    lateinit var skipPaymentAuthController:
            Controller<ProcessPaymentAuthInputModel, ProcessPaymentAuthOutputModel, ContractViewModel>
        private set

    lateinit var smsSessionRetryController:
            Controller<SmsSessionRetryInputModel, SmsSessionRetryOutputModel, ContractViewModel>
        private set

    lateinit var reporter: Reporter
        private set

    internal var isInitialized: Boolean = false

    internal fun init(
        argContext: Context,
        paymentParameters: PaymentParameters,
        testParameters: TestParameters,
        uiParameters: UiParameters
    ) {

        val context = argContext.applicationContext

        val preferencesStorage = if (testParameters.mockConfiguration == null)
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

        colorScheme = uiParameters.colorScheme
        userPhoneNumber = paymentParameters.userPhoneNumber

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
        val paymentInfoGateway: PaymentMethodInfoGateway

        googlePayIntegration = GooglePayIntegration(
            context = context,
            shopId = paymentParameters.shopId,
            useTestEnvironment = testParameters.googlePayTestEnvironment,
            loadedPaymentOptionsGateway = getLoadedPaymentOptionListGateway,
            googlePayParameters = paymentParameters.googlePayParameters
        )

        reporter = ReporterLogger(YandexMetricaReporter(context))
        val errorReporter = YandexMetricaErrorReporter(context)
        val exceptionReporter = YandexMetricaExceptionReporter(context)
        sessionReporter = YandexMetricaSessionReporter(context)

        val mockConfiguration = testParameters.mockConfiguration

        if (mockConfiguration != null) {
            val mockPaymentOptionListGateway =
                MockPaymentOptionListGateway(
                    mockConfiguration.linkedCardsCount,
                    Fee(service = mockConfiguration.serviceFee)
                )
            paymentOptionListGateway = mockPaymentOptionListGateway
            authorizeUserGateway = MockAuthorizeUserGateway
            tokenizeGateway = MockTokenizeGateway(mockConfiguration.completeWithError)
            paymentAuthTypeGateway = MockPaymentAuthTypeGateway()
            processPaymentAuthGateway = MockProcessPaymentAuthGateway()
            smsSessionRetryGateway = MockSmsSessionRetryGateway()
            walletCheckGateway = MockWalletCheckGateway()
            if (mockConfiguration.paymentAuthPassed) {
                currentUserGateway = object : CurrentUserGateway {
                    override var currentUser: CurrentUser =
                        AuthorizedUser()
                }
                userAuthTokenGateway = object : UserAuthTokenGateway {
                    override var userAuthToken: String? = "userAuthToken"
                    override var passportAuthToken: String? = "passportAuthToken"
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
            checkGooglePayAvailableGateway = StubCheckGooglePayAvailableGateway(true)
            paymentInfoGateway = MockPaymentInfoGateway()
        } else {
            currentUserGateway = SharedPreferencesCurrentUserGateway(sharedPreferences)
            val httpClient = lazy {
                try {
                    ProviderInstaller.installIfNeeded(context)
                } catch (e: Exception) {
                    errorReporter.report(SdkException(e))
                }
                newHttpClient(context)
            }
            val apiV3PaymentOptionListGateway = ApiV3PaymentOptionListGateway(
                httpClient = httpClient,
                gatewayId = paymentParameters.gatewayId,
                tokensStorage = tokensStorage,
                shopToken = paymentParameters.clientApplicationKey,
                savePaymentMethod = paymentParameters.savePaymentMethod
            )
            paymentOptionListGateway = InternetDependentGateway(context, apiV3PaymentOptionListGateway)
            if (paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YANDEX_MONEY)) {
                authorizeUserGateway = YandexAuthorizeUserGateway(
                    mainExecutor,
                    requireNotNull(paymentParameters.authCenterClientId)
                )
                yandexAuthGateway = authorizeUserGateway
            } else {
                authorizeUserGateway = object : AuthorizeUserGateway {
                    override fun authorizeUser(): AuthorizeUserGateway.User? = null
                }
            }
            val profilingTool = ThreatMetrixProfilingTool()
            profilingTool.init(context)
            tokenizeGateway = ApiV3TokenizeGateway(
                httpClient = httpClient,
                shopToken = paymentParameters.clientApplicationKey,
                paymentAuthTokenGateway = tokensStorage,
                tmxProfilingTool = profilingTool,
                tmxSessionIdStorage = tmxSessionIdStorage
            )
            val paymentAuthGateway = ApiV3PaymentAuthGateway(
                httpClient = httpClient,
                tokensStorage = tokensStorage,
                shopToken = paymentParameters.clientApplicationKey,
                tmxProfilingTool = profilingTool,
                tmxSessionIdStorage = tmxSessionIdStorage,
                selectAppropriateAuthType = SelectAppropriateAuthType()
            )
            paymentAuthTypeGateway = paymentAuthGateway
            processPaymentAuthGateway = paymentAuthGateway
            smsSessionRetryGateway = paymentAuthGateway
            walletCheckGateway = YandexWalletCheckGateway(httpClient)
            userAuthTokenGateway = tokensStorage
            paymentAuthTokenGateway = tokensStorage
            checkPaymentAuthRequiredGateway = tokensStorage
            checkGooglePayAvailableGateway = googlePayIntegration as CheckGooglePayAvailableGateway

            paymentInfoGateway = ApiV3PaymentMethodInfoGateway(
                httpClient = httpClient,
                tokensStorage = tokensStorage,
                shopToken = paymentParameters.clientApplicationKey
            )
        }

        val paymentOptionListWithGooglePayFilterGateway = PaymentOptionListWithGooglePayFilterGateway(
            paymentOptionListGateway = paymentOptionListGateway,
            googlePayAvailableGateway = checkGooglePayAvailableGateway
        )

        val paymentOptionListUseCase = LoadPaymentOptionListUseCase(
            paymentOptionListRestrictions = paymentParameters.paymentMethodTypes,
            paymentMethodInfoGateway = paymentInfoGateway,
            paymentOptionListGateway = paymentOptionListWithGooglePayFilterGateway,
            saveLoadedPaymentOptionsListGateway = saveLoadedPaymentOptionsListGateway,
            currentUserGateway = currentUserGateway
        )

        val userAuthTypeParamProvider = UserAuthTypeParamProvider(currentUserGateway, checkPaymentAuthRequiredGateway)
        val userAuthTokenTypeParamProvider = UserAuthTokenTypeParamProvider(paymentAuthTokenGateway)
        val tokenizeSchemeParamProvider = TokenizeSchemeParamProvider()
        val paymentOptionListPresenter = PaymentOptionListPresenter(context, uiParameters.showLogo)
        val changePaymentOptionPresenter = ChangePaymentOptionPresenter(context, uiParameters.showLogo)
        val paymentOptionListProgressPresenter = PaymentOptionListProgressPresenter(uiParameters.showLogo)
        val errorPresenter = ErrorPresenter(context)
        val paymentErrorPresenter = PaymentErrorPresenter(errorPresenter)
        val paymentOptionListErrorPresenter = PaymentOptionListErrorPresenter(
            context = context,
            showLogo = uiParameters.showLogo,
            errorPresenter = paymentErrorPresenter
        )
        val contractPresenter = ContractPresenter(
            context = context,
            shopTitle = paymentParameters.title,
            shopSubtitle = paymentParameters.subtitle,
            getSavePaymentMethodMessageLink = { ContractFormatter.getSavePaymentMethodMessageLink(context, it) },
            getSavePaymentMethodSwitchLink = { ContractFormatter.getSavePaymentMethodSwitchLink(context, it) },
            requestedSavePaymentMethod = paymentParameters.savePaymentMethod
        )
        val contractErrorPresenter = ContractErrorPresenter(errorPresenter)
        val requestPaymentAuthErrorPresenter = RequestPaymentAuthErrorPresenter(context, errorPresenter)

        val logger = ExceptionReportingLogger(
            logger = ErrorReportingLogger(
                logger = if (testParameters.showLogs) MsdkLogger() else StubLogger(),
                errorReporter = errorReporter
            ),
            exceptionReporter = exceptionReporter
        )

        loadPaymentOptionListController = Controller(
            name = "Load payment option list",
            useCase = ResetTokenizeSchemeWrapper(
                useCase = paymentOptionListUseCase,
                setTokenizeScheme = tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            presenter = PaymentOptionListOpenedReporter(
                getUserAuthType = userAuthTypeParamProvider,
                presenter = paymentOptionListPresenter,
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = paymentOptionListErrorPresenter,
                reporter = reporter
            ),
            progressPresenter = paymentOptionListProgressPresenter,
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        changePaymentOptionController = Controller(
            name = "Change payment option",
            useCase = ResetTokenizeSchemeWrapper(
                useCase = ChangePaymentOptionUseCase(getLoadedPaymentOptionListGateway),
                setTokenizeScheme = tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            presenter = ActionChangePaymentMethodReporter(
                presenter = changePaymentOptionPresenter,
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = paymentOptionListErrorPresenter,
                reporter = reporter
            ),
            progressPresenter = paymentOptionListProgressPresenter,
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        selectPaymentOptionController = Controller(
            name = "Select payment option",
            useCase = SelectPaymentOptionTokenizeSchemeSetter(
                SelectPaymentOptionUseCase(
                    getLoadedPaymentOptionListGateway = getLoadedPaymentOptionListGateway,
                    checkPaymentAuthRequiredGateway = checkPaymentAuthRequiredGateway
                ),
                tokenizeSchemeParamProvider::tokenizeScheme::set
            ),
            presenter = ContractOpenedReporter(
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
            progressPresenter = { ContractProgressViewModel },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        tokenizeController = Controller(
            name = "Tokenize",
            useCase = TokenizeUseCase(
                getLoadedPaymentOptionListGateway = getLoadedPaymentOptionListGateway,
                tokenizeGateway = tokenizeGateway,
                checkPaymentAuthRequiredGateway = checkPaymentAuthRequiredGateway,
                getConfirmation = {
                    it.getConfirmation(paymentParameters.customReturnUrl ?: DEFAULT_REDIRECT_URL)
                }
            ),
            presenter = ActionTokenizeReporter(
                getUserAuthType = userAuthTypeParamProvider,
                getUserAuthTokenType = userAuthTokenTypeParamProvider,
                presenter = NewBankCardScreenOpenedReporter(
                    getAuthType = userAuthTypeParamProvider,
                    presenter = LinkedCardScreenOpenedReporter(
                        presenter = RecurringCardScreenOpenedReporter(
                            presenter = PaymentOptionInfoPresenter(contractPresenter::invoke),
                            reporter = reporter
                        ),
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
            progressPresenter = { ContractProgressViewModel },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        userAuthController = Controller(
            name = "User auth",
            useCase = UserAuthUseCase(
                authorizeUserGateway = authorizeUserGateway,
                currentUserGateway = currentUserGateway,
                userAuthTokenGateway = userAuthTokenGateway
            ),
            presenter = ActionYaLoginAuthorizationReporter(
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
            progressPresenter = { UserAuthProgressViewModel },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        logoutController = Controller(
            name = "Logout",
            useCase = LogoutUseCase(
                LogoutGatewayImpl(
                    currentUserGateway = currentUserGateway,
                    userAuthTokenGateway = userAuthTokenGateway,
                    paymentAuthTokenGateway = paymentAuthTokenGateway,
                    tmxSessionIdStorage = tmxSessionIdStorage,
                    removeKeys = {
                        ivStorage.remove(ivKey)
                        keyStorage.value.remove(keyKey)
                        encrypt.reset()
                        decrypt.reset()
                    },
                    revokeUserAuthToken = { token ->
                        if (token != null && paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YANDEX_MONEY)) {
                            YooMoneyAuth.logout(argContext, token)
                        }
                    }
                )
            ),
            presenter = ActionLogoutReporter({ LogoutSuccessViewModel() }, reporter),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = { LogoutFailViewModel(errorPresenter(it)) },
                reporter = reporter
            ),
            progressPresenter = { LogoutProgressViewModel() },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        requestPaymentAuthController = Controller(
            name = "Request payment auth",
            useCase = RequestPaymentAuthUseCase(paymentAuthTypeGateway),
            presenter = contractPresenter::invoke,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = requestPaymentAuthErrorPresenter,
                reporter = reporter
            ),
            progressPresenter = { contractPresenter(RequestPaymentAuthProgressViewModel) },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        processPaymentAuthController = Controller(
            name = "Process payment auth",
            useCase = ProcessPaymentAuthUseCase(
                processPaymentAuthGateway = processPaymentAuthGateway,
                currentUserGateway = currentUserGateway,
                paymentAuthTokenGateway = paymentAuthTokenGateway
            ),
            presenter = ActionPaymentAuthorizationReporter(
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
            progressPresenter = { contractPresenter(ProcessPaymentAuthProgressViewModel) },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        smsSessionRetryController = Controller(
            name = "Sms session retry",
            useCase = SmsSessionRetryUseCase(smsSessionRetryGateway),
            presenter = contractPresenter::invoke,
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = requestPaymentAuthErrorPresenter,
                reporter = reporter
            ),
            progressPresenter = { contractPresenter(SmsSessionRetryProgressViewModel) },
            resultConsumer = listeners::onEvent,
            logger = logger
        )

        isInitialized = true
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
