/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
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

package ru.yoo.sdk.kassa.payments.impl

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.security.ProviderInstaller
import ru.yoo.sdk.kassa.payments.PaymentMethodType
import ru.yoo.sdk.kassa.payments.PaymentParameters
import ru.yoo.sdk.kassa.payments.TestParameters
import ru.yoo.sdk.kassa.payments.UiParameters
import ru.yoo.sdk.kassa.payments.impl.InMemoryColorSchemeRepository.colorScheme
import ru.yoo.sdk.kassa.payments.impl.contract.ContractErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.contract.ContractFormatter
import ru.yoo.sdk.kassa.payments.impl.contract.ContractPresenter
import ru.yoo.sdk.kassa.payments.impl.contract.ContractProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.contract.ContractViewModel
import ru.yoo.sdk.kassa.payments.impl.extensions.getConfirmation
import ru.yoo.sdk.kassa.payments.impl.logging.MsdkLogger
import ru.yoo.sdk.kassa.payments.impl.logging.ReporterLogger
import ru.yoo.sdk.kassa.payments.impl.logging.StubLogger
import ru.yoo.sdk.kassa.payments.impl.logout.LogoutFailViewModel
import ru.yoo.sdk.kassa.payments.impl.logout.LogoutGatewayImpl
import ru.yoo.sdk.kassa.payments.impl.logout.LogoutProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.logout.LogoutSuccessViewModel
import ru.yoo.sdk.kassa.payments.impl.logout.LogoutViewModel
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionChangePaymentMethodReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionLogoutReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionPaymentAuthorizationErrorReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionPaymentAuthorizationReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionTokenizeReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionLoginAuthorizationFailedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ActionYooMoneyLoginAuthorizationReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ContractOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ErrorReportingLogger
import ru.yoo.sdk.kassa.payments.impl.metrics.ErrorScreenOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ExceptionReportingLogger
import ru.yoo.sdk.kassa.payments.impl.metrics.LinkedCardScreenOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.NewBankCardScreenOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.PaymentOptionListOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.RecurringCardScreenOpenedReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.Reporter
import ru.yoo.sdk.kassa.payments.impl.metrics.ResetTokenizeSchemeWrapper
import ru.yoo.sdk.kassa.payments.impl.metrics.SelectPaymentOptionTokenizeSchemeSetter
import ru.yoo.sdk.kassa.payments.impl.metrics.SessionReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.TokenizeSchemeParamProvider
import ru.yoo.sdk.kassa.payments.impl.metrics.UserAuthTokenTypeParamProvider
import ru.yoo.sdk.kassa.payments.impl.metrics.UserAuthTypeParamProvider
import ru.yoo.sdk.kassa.payments.impl.metrics.YandexMetricaErrorReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.YandexMetricaExceptionReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.YandexMetricaReporter
import ru.yoo.sdk.kassa.payments.impl.metrics.YandexMetricaSessionReporter
import ru.yoo.sdk.kassa.payments.impl.payment.PaymentErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.payment.SharedPreferencesCurrentUserGateway
import ru.yoo.sdk.kassa.payments.impl.payment.tokenize.ApiV3TokenizeGateway
import ru.yoo.sdk.kassa.payments.impl.payment.tokenize.MockTokenizeGateway
import ru.yoo.sdk.kassa.payments.impl.payment.tokenize.TokenizeErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.ApiV3PaymentAuthGateway
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.MockPaymentAuthTypeGateway
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.MockProcessPaymentAuthGateway
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.MockSmsSessionRetryGateway
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.ProcessPaymentAuthErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.ProcessPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.RequestPaymentAuthErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.RequestPaymentAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.SelectAppropriateAuthType
import ru.yoo.sdk.kassa.payments.impl.paymentAuth.SmsSessionRetryProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentMethodInfo.ApiV3PaymentMethodInfoGateway
import ru.yoo.sdk.kassa.payments.impl.paymentMethodInfo.MockPaymentInfoGateway
import ru.yoo.sdk.kassa.payments.impl.paymentOptionInfo.PaymentOptionInfoPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.ApiV3PaymentOptionListGateway
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.ChangePaymentOptionPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.GooglePayIntegration
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.InternetDependentGateway
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.MockPaymentOptionListGateway
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.PaymentOptionListErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.PaymentOptionListPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.PaymentOptionListProgressPresenter
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.PaymentOptionListViewModel
import ru.yoo.sdk.kassa.payments.impl.paymentOptionList.StubCheckGooglePayAvailableGateway
import ru.yoo.sdk.kassa.payments.impl.secure.BcKeyStorage
import ru.yoo.sdk.kassa.payments.impl.secure.Decrypter
import ru.yoo.sdk.kassa.payments.impl.secure.Encrypter
import ru.yoo.sdk.kassa.payments.impl.secure.SharedPreferencesIvStorage
import ru.yoo.sdk.kassa.payments.impl.secure.getPlatformPassword
import ru.yoo.sdk.kassa.payments.impl.userAuth.MockAuthorizeUserGateway
import ru.yoo.sdk.kassa.payments.impl.userAuth.MockWalletCheckGateway
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthErrorPresenter
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthPresenter
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthProgressViewModel
import ru.yoo.sdk.kassa.payments.impl.userAuth.UserAuthViewModel
import ru.yoo.sdk.kassa.payments.impl.userAuth.YooMoneyAuthorizeUserGateway
import ru.yoo.sdk.kassa.payments.impl.userAuth.YooMoneyWalletCheckGateway
import ru.yoo.sdk.kassa.payments.logout.LogoutInputModel
import ru.yoo.sdk.kassa.payments.logout.LogoutOutputModel
import ru.yoo.sdk.kassa.payments.logout.LogoutUseCase
import ru.yoo.sdk.kassa.payments.model.AuthorizedUser
import ru.yoo.sdk.kassa.payments.model.Controller
import ru.yoo.sdk.kassa.payments.model.CurrentUser
import ru.yoo.sdk.kassa.payments.model.Fee
import ru.yoo.sdk.kassa.payments.model.SdkException
import ru.yoo.sdk.kassa.payments.model.StateHolder
import ru.yoo.sdk.kassa.payments.model.ViewModel
import ru.yoo.sdk.kassa.payments.model.newHttpClient
import ru.yoo.sdk.kassa.payments.payment.CheckPaymentAuthRequiredGateway
import ru.yoo.sdk.kassa.payments.payment.CurrentUserGateway
import ru.yoo.sdk.kassa.payments.payment.GetLoadedPaymentOptionListGateway
import ru.yoo.sdk.kassa.payments.payment.InMemoryPaymentOptionListGateway
import ru.yoo.sdk.kassa.payments.payment.SaveLoadedPaymentOptionsListGateway
import ru.yoo.sdk.kassa.payments.payment.changeOption.ChangePaymentOptionInputModel
import ru.yoo.sdk.kassa.payments.payment.changeOption.ChangePaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.changeOption.ChangePaymentOptionUseCase
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.CheckGooglePayAvailableGateway
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.LoadPaymentOptionListUseCase
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListGateway
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListInputModel
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListOutputModel
import ru.yoo.sdk.kassa.payments.payment.loadOptionList.PaymentOptionListWithGooglePayFilterGateway
import ru.yoo.sdk.kassa.payments.payment.loadPaymentInfo.PaymentMethodInfoGateway
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionInputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionOutputModel
import ru.yoo.sdk.kassa.payments.payment.selectOption.SelectPaymentOptionUseCase
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeGateway
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeInputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeOutputModel
import ru.yoo.sdk.kassa.payments.payment.tokenize.TokenizeUseCase
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthGateway
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.ProcessPaymentAuthUseCase
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.RequestPaymentAuthUseCase
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryInputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryOutputModel
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryUseCase
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthInputModel
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthOutputModel
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthUseCase
import ru.yoo.sdk.kassa.payments.userAuth.WalletCheckGateway
import ru.yoo.sdk.auth.YooMoneyAuth
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTokenGateway
import ru.yoo.sdk.kassa.payments.paymentAuth.PaymentAuthTypeGateway
import ru.yoo.sdk.kassa.payments.paymentAuth.SmsSessionRetryGateway
import ru.yoo.sdk.kassa.payments.userAuth.AuthorizeUserGateway
import ru.yoo.sdk.kassa.payments.userAuth.UserAuthTokenGateway

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

    var yooMoneyAuthGateway: YooMoneyAuthorizeUserGateway? = null
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
                currentUserGateway = SharedPreferencesCurrentUserGateway(tokensStorage, sharedPreferences)
                userAuthTokenGateway = tokensStorage
                paymentAuthTokenGateway = tokensStorage
                checkPaymentAuthRequiredGateway = tokensStorage
            }
            checkGooglePayAvailableGateway = StubCheckGooglePayAvailableGateway(true)
            paymentInfoGateway = MockPaymentInfoGateway()
        } else {
            currentUserGateway = SharedPreferencesCurrentUserGateway(tokensStorage, sharedPreferences)
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
            if (paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY)) {
                authorizeUserGateway = YooMoneyAuthorizeUserGateway(
                    mainExecutor,
                    requireNotNull(paymentParameters.authCenterClientId)
                )
                yooMoneyAuthGateway = authorizeUserGateway
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
            walletCheckGateway = YooMoneyWalletCheckGateway(httpClient)
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
            presenter = ActionYooMoneyLoginAuthorizationReporter(
                presenter = UserAuthPresenter(),
                reporter = reporter
            ),
            errorPresenter = ErrorScreenOpenedReporter(
                getAuthType = userAuthTypeParamProvider,
                getTokenizeScheme = tokenizeSchemeParamProvider,
                presenter = ActionLoginAuthorizationFailedReporter(
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
                        if (token != null && paymentParameters.paymentMethodTypes.contains(PaymentMethodType.YOO_MONEY)) {
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
            useCase = RequestPaymentAuthUseCase(
                paymentAuthTypeGateway
            ),
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
            useCase = SmsSessionRetryUseCase(
                smsSessionRetryGateway
            ),
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

        yooMoneyAuthGateway?.reset()
        googlePayIntegration?.reset()
        tokensStorage.reset()
    }
}
