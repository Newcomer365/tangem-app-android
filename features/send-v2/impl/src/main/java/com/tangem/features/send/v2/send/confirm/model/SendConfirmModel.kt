package com.tangem.features.send.v2.send.confirm.model

import android.os.SystemClock
import androidx.compose.runtime.Stable
import arrow.core.getOrElse
import com.tangem.blockchain.common.AmountType
import com.tangem.blockchain.common.TransactionData
import com.tangem.common.routing.AppRouter
import com.tangem.common.ui.amountScreen.models.AmountState
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.decompose.di.ModelScoped
import com.tangem.core.decompose.model.Model
import com.tangem.core.decompose.model.ParamsContainer
import com.tangem.core.decompose.navigation.Router
import com.tangem.core.navigation.share.ShareManager
import com.tangem.core.navigation.url.UrlOpener
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.domain.feedback.GetCardInfoUseCase
import com.tangem.domain.feedback.SaveBlockchainErrorUseCase
import com.tangem.domain.feedback.SendFeedbackEmailUseCase
import com.tangem.domain.feedback.models.BlockchainErrorInfo
import com.tangem.domain.feedback.models.FeedbackEmailType
import com.tangem.domain.settings.IsSendTapHelpEnabledUseCase
import com.tangem.domain.settings.NeverShowTapHelpUseCase
import com.tangem.domain.tokens.AddCryptoCurrenciesUseCase
import com.tangem.domain.tokens.IsAmountSubtractAvailableUseCase
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.transaction.usecase.CreateTransferTransactionUseCase
import com.tangem.domain.transaction.usecase.SendTransactionUseCase
import com.tangem.domain.txhistory.usecase.GetExplorerTransactionUrlUseCase
import com.tangem.domain.utils.convertToSdkAmount
import com.tangem.features.send.v2.common.CommonSendRoute
import com.tangem.features.send.v2.common.SendBalanceUpdater
import com.tangem.features.send.v2.common.SendConfirmAlertFactory
import com.tangem.features.send.v2.common.analytics.CommonSendAnalyticEvents
import com.tangem.features.send.v2.common.analytics.CommonSendAnalyticEvents.SendScreenSource
import com.tangem.features.send.v2.common.ui.state.ConfirmUM
import com.tangem.features.send.v2.common.ui.state.NavigationUM
import com.tangem.features.send.v2.impl.R
import com.tangem.features.send.v2.send.analytics.SendAnalyticHelper
import com.tangem.features.send.v2.send.confirm.SendConfirmComponent
import com.tangem.features.send.v2.send.confirm.model.transformers.SendConfirmInitialStateTransformer
import com.tangem.features.send.v2.send.confirm.model.transformers.SendConfirmSendingStateTransformer
import com.tangem.features.send.v2.send.confirm.model.transformers.SendConfirmSentStateTransformer
import com.tangem.features.send.v2.send.confirm.model.transformers.SendConfirmationNotificationsTransformer
import com.tangem.features.send.v2.send.ui.state.ButtonsUM
import com.tangem.features.send.v2.send.ui.state.SendUM
import com.tangem.features.send.v2.subcomponents.destination.ui.state.DestinationUM
import com.tangem.features.send.v2.subcomponents.fee.SendFeeCheckReloadListener
import com.tangem.features.send.v2.subcomponents.fee.SendFeeCheckReloadTrigger
import com.tangem.features.send.v2.subcomponents.fee.model.checkAndCalculateSubtractedAmount
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeSelectorUM
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeUM
import com.tangem.features.send.v2.subcomponents.notifications.NotificationsUpdateTrigger
import com.tangem.features.send.v2.subcomponents.notifications.model.NotificationData
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import com.tangem.utils.extensions.orZero
import com.tangem.utils.extensions.stripZeroPlainString
import com.tangem.utils.transformer.update
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass")
@Stable
@ModelScoped
internal class SendConfirmModel @Inject constructor(
    paramsContainer: ParamsContainer,
    override val dispatchers: CoroutineDispatcherProvider,
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val appRouter: AppRouter,
    private val router: Router,
    private val isSendTapHelpEnabledUseCase: IsSendTapHelpEnabledUseCase,
    private val neverShowTapHelpUseCase: NeverShowTapHelpUseCase,
    private val createTransferTransactionUseCase: CreateTransferTransactionUseCase,
    private val sendTransactionUseCase: SendTransactionUseCase,
    private val saveBlockchainErrorUseCase: SaveBlockchainErrorUseCase,
    private val getCardInfoUseCase: GetCardInfoUseCase,
    private val sendFeedbackEmailUseCase: SendFeedbackEmailUseCase,
    private val addCryptoCurrenciesUseCase: AddCryptoCurrenciesUseCase,
    private val getExplorerTransactionUrlUseCase: GetExplorerTransactionUrlUseCase,
    private val isAmountSubtractAvailableUseCase: IsAmountSubtractAvailableUseCase,
    private val sendFeeCheckReloadTrigger: SendFeeCheckReloadTrigger,
    private val sendFeeCheckReloadListener: SendFeeCheckReloadListener,
    private val notificationsUpdateTrigger: NotificationsUpdateTrigger,
    private val alertFactory: SendConfirmAlertFactory,
    private val sendAnalyticHelper: SendAnalyticHelper,
    private val urlOpener: UrlOpener,
    private val shareManager: ShareManager,
    sendBalanceUpdaterFactory: SendBalanceUpdater.Factory,
) : Model(), SendConfirmClickIntents {

    private val params: SendConfirmComponent.Params = paramsContainer.require()

    private val analyticsCategoryName = params.analyticsCategoryName
    private val userWallet = params.userWallet
    private val appCurrency = params.appCurrency
    private val cryptoCurrencyStatus = params.cryptoCurrencyStatus
    private val cryptoCurrency = cryptoCurrencyStatus.currency

    private val sendBalanceUpdater = sendBalanceUpdaterFactory.create(cryptoCurrency, userWallet)

    private val _uiState = MutableStateFlow(params.state)
    val uiState = _uiState.asStateFlow()

    private val amountState
        get() = uiState.value.amountUM as? AmountState.Data
    private val destinationUM
        get() = uiState.value.destinationUM as? DestinationUM.Content
    private val feeUM
        get() = uiState.value.feeUM as? FeeUM.Content
    private val feeSelectorUM
        get() = feeUM?.feeSelectorUM as? FeeSelectorUM.Content

    val confirmData: ConfirmData
        get() = ConfirmData(
            enteredAmount = amountState?.amountTextField?.cryptoAmount?.value,
            enteredMemo = destinationUM?.memoTextField?.value,
            reduceAmountBy = amountState?.reduceAmountBy.orZero(),
            isIgnoreReduce = amountState?.isIgnoreReduce == true,
            enteredDestination = destinationUM?.addressTextField?.value,
            fee = feeSelectorUM?.selectedFee,
            feeError = (feeUM?.feeSelectorUM as? FeeSelectorUM.Error)?.error,
        )

    private var sendIdleTimer: Long = 0L
    private var isAmountSubtractAvailable = false

    init {
        modelScope.launch {
            isAmountSubtractAvailable =
                isAmountSubtractAvailableUseCase(userWallet.walletId, cryptoCurrency).getOrElse { false }
        }
        configConfirmNavigation()
        subscribeOnNotificationsUpdateTrigger()
        subscribeOnCheckFeeResultUpdates()
        initialState()
    }

    fun updateState(state: SendUM) {
        _uiState.value = state
        updateConfirmNotifications()
    }

    fun onFeeResult(feeUM: FeeUM) {
        sendIdleTimer = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(feeUM = feeUM) }
        updateConfirmNotifications()
    }

    fun onAmountResult(amountUM: AmountState) {
        _uiState.update { it.copy(amountUM = amountUM) }
        updateConfirmNotifications()
    }

    fun onDestinationResult(destinationUM: DestinationUM) {
        _uiState.update { it.copy(destinationUM = destinationUM) }
        updateConfirmNotifications()
    }

    override fun showEditDestination() {
        modelScope.launch {
            neverShowTapHelpUseCase()
            _uiState.update {
                val confirmUM = it.confirmUM as? ConfirmUM.Content
                it.copy(confirmUM = confirmUM?.copy(showTapHelp = false) ?: it.confirmUM)
            }
            analyticsEventHandler.send(
                CommonSendAnalyticEvents.ScreenReopened(
                    categoryName = analyticsCategoryName,
                    source = SendScreenSource.Address,
                ),
            )
            router.push(CommonSendRoute.Destination(isEditMode = true))
        }
    }

    override fun showEditAmount() {
        modelScope.launch {
            neverShowTapHelpUseCase()
            _uiState.update {
                val confirmUM = it.confirmUM as? ConfirmUM.Content
                it.copy(confirmUM = confirmUM?.copy(showTapHelp = false) ?: it.confirmUM)
            }
            analyticsEventHandler.send(
                CommonSendAnalyticEvents.ScreenReopened(
                    categoryName = analyticsCategoryName,
                    source = SendScreenSource.Amount,
                ),
            )
            router.push(CommonSendRoute.Amount(isEditMode = true))
        }
    }

    override fun showEditFee() {
        modelScope.launch {
            neverShowTapHelpUseCase()
            _uiState.update {
                val confirmUM = it.confirmUM as? ConfirmUM.Content
                it.copy(confirmUM = confirmUM?.copy(showTapHelp = false) ?: it.confirmUM)
            }
            analyticsEventHandler.send(
                CommonSendAnalyticEvents.ScreenReopened(
                    categoryName = analyticsCategoryName,
                    source = SendScreenSource.Fee,
                ),
            )
            router.push(CommonSendRoute.Fee)
        }
    }

    override fun onSendClick() {
        _uiState.update(SendConfirmSendingStateTransformer(isSending = true))
        if (SystemClock.elapsedRealtime() - sendIdleTimer < CHECK_FEE_UPDATE_DELAY) {
            verifyAndSendTransaction()
        } else {
            modelScope.launch {
                sendFeeCheckReloadTrigger.triggerCheckUpdate()
            }
        }
    }

    override fun onExploreClick() {
        val confirmUM = uiState.value.confirmUM as? ConfirmUM.Success ?: return
        analyticsEventHandler.send(CommonSendAnalyticEvents.ExploreButtonClicked(analyticsCategoryName))
        urlOpener.openUrl(confirmUM.txUrl)
    }

    override fun onShareClick() {
        val confirmUM = uiState.value.confirmUM as? ConfirmUM.Success ?: return
        analyticsEventHandler.send(CommonSendAnalyticEvents.ShareButtonClicked(analyticsCategoryName))
        shareManager.shareText(confirmUM.txUrl)
    }

    override fun onFailedTxEmailClick(errorMessage: String) {
        val amountValue = amountState?.amountTextField?.cryptoAmount?.value
        val feeValue = confirmData.fee?.amount?.value

        val receivingAmount = if (amountValue != null && feeValue != null) {
            checkAndCalculateSubtractedAmount(
                isAmountSubtractAvailable = isAmountSubtractAvailable,
                cryptoCurrencyStatus = cryptoCurrencyStatus,
                amountValue = confirmData.enteredAmount.orZero(),
                feeValue = feeValue,
                reduceAmountBy = confirmData.reduceAmountBy,
            )
        } else {
            null
        }

        val amount = receivingAmount?.convertToSdkAmount(cryptoCurrency)

        saveBlockchainErrorUseCase(
            error = BlockchainErrorInfo(
                errorMessage = errorMessage,
                blockchainId = cryptoCurrency.network.id.value,
                derivationPath = cryptoCurrency.network.derivationPath.value,
                destinationAddress = confirmData.enteredDestination.orEmpty(),
                tokenSymbol = if (amount?.type is AmountType.Token) {
                    amount.currencySymbol
                } else {
                    ""
                },
                amount = amount?.value?.stripZeroPlainString() ?: "unknown",
                fee = feeValue?.convertToSdkAmount(cryptoCurrency)
                    ?.value?.stripZeroPlainString() ?: "unknown",
            ),
        )

        val cardInfo = getCardInfoUseCase(userWallet.scanResponse).getOrNull() ?: return

        modelScope.launch {
            sendFeedbackEmailUseCase(type = FeedbackEmailType.TransactionSendingProblem(cardInfo = cardInfo))
        }
    }

    private fun initialState() {
        val confirmUM = uiState.value.confirmUM

        modelScope.launch {
            val isShowTapHelp = isSendTapHelpEnabledUseCase().getOrElse { false }
            if (confirmUM is ConfirmUM.Empty) {
                _uiState.update {
                    it.copy(
                        confirmUM = SendConfirmInitialStateTransformer(
                            isShowTapHelp = isShowTapHelp,
                        ).transform(uiState.value.confirmUM),
                    )
                }
                updateConfirmNotifications()
            }
        }
    }

    private fun subscribeOnNotificationsUpdateTrigger() {
        notificationsUpdateTrigger.hasErrorFlow
            .onEach { hasError ->
                _uiState.update {
                    val feeUM = it.feeUM as? FeeUM.Content
                    val feeSelectorUM = feeUM?.feeSelectorUM as? FeeSelectorUM.Content
                    it.copy(
                        confirmUM = (it.confirmUM as? ConfirmUM.Content)?.copy(
                            isPrimaryButtonEnabled = !hasError && feeSelectorUM != null,
                        ) ?: it.confirmUM,
                    )
                }
            }
            .launchIn(modelScope)
    }

    private fun verifyAndSendTransaction() {
        val amountValue = amountState?.amountTextField?.cryptoAmount?.value ?: return
        val destination = destinationUM?.addressTextField?.value ?: return
        val memo = destinationUM?.memoTextField?.value
        val fee = feeSelectorUM?.selectedFee
        val feeValue = fee?.amount?.value ?: return

        val receivingAmount = checkAndCalculateSubtractedAmount(
            isAmountSubtractAvailable = isAmountSubtractAvailable,
            cryptoCurrencyStatus = cryptoCurrencyStatus,
            amountValue = amountValue,
            feeValue = feeValue,
            reduceAmountBy = confirmData.reduceAmountBy.orZero(),
        )

        modelScope.launch {
            createTransferTransactionUseCase(
                amount = receivingAmount.convertToSdkAmount(cryptoCurrency),
                fee = fee,
                memo = memo,
                destination = destination,
                userWalletId = userWallet.walletId,
                network = cryptoCurrency.network,
            ).fold(
                ifLeft = { error ->
                    Timber.e(error)
                    _uiState.update(SendConfirmSendingStateTransformer(isSending = false))
                    alertFactory.getGenericErrorState(
                        onFailedTxEmailClick = {
                            onFailedTxEmailClick(error.localizedMessage.orEmpty())
                        },
                    )
                },
                ifRight = { txData ->
                    sendTransaction(txData)
                },
            )
        }
    }

    private suspend fun sendTransaction(txData: TransactionData.Uncompiled) {
        val result = sendTransactionUseCase(
            txData = txData,
            userWallet = userWallet,
            network = cryptoCurrency.network,
        )

        _uiState.update(SendConfirmSendingStateTransformer(isSending = false))

        result.fold(
            ifLeft = { error ->
                alertFactory.getSendTransactionErrorState(
                    error = error,
                    popBack = appRouter::pop,
                    onFailedTxEmailClick = ::onFailedTxEmailClick,
                )
                analyticsEventHandler.send(
                    CommonSendAnalyticEvents.TransactionError(
                        categoryName = analyticsCategoryName,
                        token = cryptoCurrency.symbol,
                    ),
                )
            },
            ifRight = {
                updateTransactionStatus(txData)
                addTokenToWalletIfNeeded()
                sendBalanceUpdater.scheduleUpdates()
                sendAnalyticHelper.sendSuccessAnalytics(cryptoCurrency, uiState.value)
            },
        )
    }

    private fun addTokenToWalletIfNeeded() {
        if (cryptoCurrency !is CryptoCurrency.Token) return
        val wallets = destinationUM?.wallets ?: return

        val receivingUserWallet = wallets
            .firstOrNull { it.address == confirmData.enteredDestination }
            ?: return

        val userWalletId = receivingUserWallet.userWalletId ?: return
        val network = receivingUserWallet.network ?: return

        modelScope.launch {
            addCryptoCurrenciesUseCase(
                userWalletId = userWalletId,
                cryptoCurrency = cryptoCurrency,
                network = network,
            )
        }
    }

    private fun updateTransactionStatus(txData: TransactionData.Uncompiled) {
        val txUrl = getExplorerTransactionUrlUseCase(
            txHash = txData.hash.orEmpty(),
            networkId = cryptoCurrency.network.id,
        ).getOrElse { "" }
        _uiState.update(SendConfirmSentStateTransformer(txData, txUrl))
    }

    private fun subscribeOnCheckFeeResultUpdates() {
        sendFeeCheckReloadListener.checkReloadResultFlow.onEach { isFeeResultSuccess ->
            if (isFeeResultSuccess) {
                sendIdleTimer = SystemClock.elapsedRealtime()
                _uiState.update(SendConfirmSendingStateTransformer(isSending = true))
                verifyAndSendTransaction()
            } else {
                _uiState.update(SendConfirmSendingStateTransformer(isSending = false))
            }
        }.launchIn(modelScope)
    }

    private fun updateConfirmNotifications() {
        modelScope.launch {
            notificationsUpdateTrigger.triggerUpdate(
                data = NotificationData(
                    destinationAddress = confirmData.enteredDestination.orEmpty(),
                    memo = confirmData.enteredMemo,
                    amountValue = confirmData.enteredAmount.orZero(),
                    reduceAmountBy = confirmData.reduceAmountBy.orZero(),
                    isIgnoreReduce = confirmData.isIgnoreReduce,
                    fee = confirmData.fee,
                    feeError = confirmData.feeError,
                ),
            )
            _uiState.update {
                it.copy(
                    confirmUM = SendConfirmationNotificationsTransformer(
                        feeUM = uiState.value.feeUM,
                        amountUM = uiState.value.amountUM,
                        analyticsEventHandler = analyticsEventHandler,
                        cryptoCurrency = cryptoCurrencyStatus.currency,
                        appCurrency = appCurrency,
                        analyticsCategoryName = params.analyticsCategoryName,
                    ).transform(uiState.value.confirmUM),
                )
            }
        }
    }

    private fun configConfirmNavigation() {
        combine(
            flow = uiState,
            flow2 = params.currentRoute,
            transform = { state, route -> state to route },
        ).filter { it.second is CommonSendRoute.Confirm }.onEach { (state, _) ->
            val amountUM = state.amountUM as? AmountState.Data
            val confirmUM = state.confirmUM
            val isReadyToSend = confirmUM is ConfirmUM.Content && !confirmUM.isSending
            params.callback.onResult(
                state.copy(
                    navigationUM = NavigationUM.Content(
                        title = resourceReference(
                            id = R.string.send_summary_title,
                            formatArgs = wrappedList(params.cryptoCurrencyStatus.currency.name),
                        ),
                        subtitle = amountUM?.title,
                        backIconRes = R.drawable.ic_close_24,
                        backIconClick = {
                            analyticsEventHandler.send(
                                CommonSendAnalyticEvents.CloseButtonClicked(
                                    categoryName = analyticsCategoryName,
                                    source = SendScreenSource.Confirm,
                                    isFromSummary = true,
                                    isValid = confirmUM.isPrimaryButtonEnabled,
                                ),
                            )
                            appRouter.pop()
                        },
                        primaryButton = ButtonsUM.PrimaryButtonUM(
                            text = when (confirmUM) {
                                is ConfirmUM.Success -> resourceReference(R.string.common_close)
                                is ConfirmUM.Content -> if (confirmUM.isSending) {
                                    resourceReference(R.string.send_sending)
                                } else {
                                    resourceReference(R.string.common_send)
                                }
                                else -> resourceReference(R.string.common_send)
                            },
                            iconResId = R.drawable.ic_tangem_24.takeIf { isReadyToSend },
                            isEnabled = confirmUM.isPrimaryButtonEnabled,
                            isHapticClick = isReadyToSend,
                            onClick = {
                                when (confirmUM) {
                                    is ConfirmUM.Success -> appRouter.pop()
                                    is ConfirmUM.Content -> if (confirmUM.isSending) {
                                        return@PrimaryButtonUM
                                    } else {
                                        onSendClick()
                                    }
                                    else -> return@PrimaryButtonUM
                                }
                            },
                        ),
                        prevButton = null,
                        secondaryPairButtonsUM = ButtonsUM.SecondaryPairButtonsUM(
                            leftText = resourceReference(R.string.common_explore),
                            leftIconResId = R.drawable.ic_web_24,
                            onLeftClick = ::onExploreClick,
                            rightText = resourceReference(R.string.common_share),
                            rightIconResId = R.drawable.ic_share_24,
                            onRightClick = ::onShareClick,
                        ).takeIf { confirmUM is ConfirmUM.Success },
                    ),
                ),
            )
        }.launchIn(modelScope)
    }

    private companion object {
        const val CHECK_FEE_UPDATE_DELAY = 10_000L
    }
}
