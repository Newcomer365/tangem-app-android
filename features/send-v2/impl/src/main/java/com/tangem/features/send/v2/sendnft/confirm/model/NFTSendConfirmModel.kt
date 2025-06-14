package com.tangem.features.send.v2.sendnft.confirm.model

import android.os.SystemClock
import arrow.core.getOrElse
import com.tangem.blockchain.common.TransactionData
import com.tangem.common.routing.AppRouter
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.decompose.di.ModelScoped
import com.tangem.core.decompose.model.Model
import com.tangem.core.decompose.model.ParamsContainer
import com.tangem.core.decompose.navigation.Router
import com.tangem.core.navigation.share.ShareManager
import com.tangem.core.navigation.url.UrlOpener
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.datasource.local.nft.converter.NFTSdkAssetConverter
import com.tangem.domain.feedback.GetCardInfoUseCase
import com.tangem.domain.feedback.SaveBlockchainErrorUseCase
import com.tangem.domain.feedback.SendFeedbackEmailUseCase
import com.tangem.domain.feedback.models.BlockchainErrorInfo
import com.tangem.domain.feedback.models.FeedbackEmailType
import com.tangem.domain.settings.IsSendTapHelpEnabledUseCase
import com.tangem.domain.settings.NeverShowTapHelpUseCase
import com.tangem.domain.transaction.usecase.CreateNFTTransferTransactionUseCase
import com.tangem.domain.transaction.usecase.SendTransactionUseCase
import com.tangem.domain.txhistory.usecase.GetExplorerTransactionUrlUseCase
import com.tangem.features.send.v2.common.CommonSendRoute
import com.tangem.features.send.v2.common.SendBalanceUpdater
import com.tangem.features.send.v2.common.SendConfirmAlertFactory
import com.tangem.features.send.v2.common.analytics.CommonSendAnalyticEvents
import com.tangem.features.send.v2.common.analytics.CommonSendAnalyticEvents.SendScreenSource
import com.tangem.features.send.v2.common.ui.state.ConfirmUM
import com.tangem.features.send.v2.common.ui.state.NavigationUM
import com.tangem.features.send.v2.impl.R
import com.tangem.features.send.v2.send.ui.state.ButtonsUM
import com.tangem.features.send.v2.sendnft.analytics.NFTSendAnalyticHelper
import com.tangem.features.send.v2.sendnft.confirm.NFTSendConfirmComponent
import com.tangem.features.send.v2.sendnft.confirm.model.transformers.NFTSendConfirmInitialStateTransformer
import com.tangem.features.send.v2.sendnft.confirm.model.transformers.NFTSendConfirmSendingStateTransformer
import com.tangem.features.send.v2.sendnft.confirm.model.transformers.NFTSendConfirmationNotificationsTransformer
import com.tangem.features.send.v2.sendnft.ui.state.NFTSendUM
import com.tangem.features.send.v2.subcomponents.destination.ui.state.DestinationUM
import com.tangem.features.send.v2.subcomponents.fee.SendFeeCheckReloadListener
import com.tangem.features.send.v2.subcomponents.fee.SendFeeCheckReloadTrigger
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeSelectorUM
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeUM
import com.tangem.features.send.v2.subcomponents.notifications.NotificationsUpdateTrigger
import com.tangem.features.send.v2.subcomponents.notifications.model.NotificationData
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import com.tangem.utils.extensions.stripZeroPlainString
import com.tangem.utils.transformer.update
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass")
@ModelScoped
internal class NFTSendConfirmModel @Inject constructor(
    paramsContainer: ParamsContainer,
    override val dispatchers: CoroutineDispatcherProvider,
    private val router: Router,
    private val appRouter: AppRouter,
    private val isSendTapHelpEnabledUseCase: IsSendTapHelpEnabledUseCase,
    private val neverShowTapHelpUseCase: NeverShowTapHelpUseCase,
    private val createNFTTransferTransactionUseCase: CreateNFTTransferTransactionUseCase,
    private val sendTransactionUseCase: SendTransactionUseCase,
    private val getExplorerTransactionUrlUseCase: GetExplorerTransactionUrlUseCase,
    private val saveBlockchainErrorUseCase: SaveBlockchainErrorUseCase,
    private val getCardInfoUseCase: GetCardInfoUseCase,
    private val sendFeedbackEmailUseCase: SendFeedbackEmailUseCase,
    private val notificationsUpdateTrigger: NotificationsUpdateTrigger,
    private val sendFeeCheckReloadTrigger: SendFeeCheckReloadTrigger,
    private val sendFeeCheckReloadListener: SendFeeCheckReloadListener,
    private val alertFactory: SendConfirmAlertFactory,
    private val urlOpener: UrlOpener,
    private val shareManager: ShareManager,
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val nftSendAnalyticHelper: NFTSendAnalyticHelper,
    sendBalanceUpdaterFactory: SendBalanceUpdater.Factory,
) : Model(), NFTSendConfirmClickIntents {

    private val params: NFTSendConfirmComponent.Params = paramsContainer.require()

    private val analyticsCategoryName = params.analyticsCategoryName
    private val userWallet = params.userWallet
    private val cryptoCurrencyStatus = params.cryptoCurrencyStatus
    private val cryptoCurrency = cryptoCurrencyStatus.currency

    private val _uiState = MutableStateFlow(params.state)
    val uiState = _uiState.asStateFlow()

    private val sendBalanceUpdater = sendBalanceUpdaterFactory.create(
        cryptoCurrency = cryptoCurrency,
        userWallet = userWallet,
    )

    private val destinationUM
        get() = uiState.value.destinationUM as? DestinationUM.Content
    private val feeUM
        get() = uiState.value.feeUM as? FeeUM.Content
    private val feeSelectorUM
        get() = feeUM?.feeSelectorUM as? FeeSelectorUM.Content

    val confirmData: ConfirmData
        get() = ConfirmData(
            enteredDestination = destinationUM?.addressTextField?.value,
            enteredMemo = destinationUM?.memoTextField?.value,
            fee = feeSelectorUM?.selectedFee,
            feeError = (feeUM?.feeSelectorUM as? FeeSelectorUM.Error)?.error,
        )

    private var sendIdleTimer: Long = 0L

    init {
        configConfirmNavigation()
        subscribeOnNotificationsUpdateTrigger()
        subscribeOnCheckFeeResultUpdates()
        initialState()
    }

    fun updateState(nftSendUM: NFTSendUM) {
        _uiState.value = nftSendUM
        updateConfirmNotifications()
    }

    fun onFeeResult(feeUM: FeeUM) {
        sendIdleTimer = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(feeUM = feeUM) }
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
        _uiState.update(NFTSendConfirmSendingStateTransformer(isSending = true))
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
        analyticsEventHandler.send(
            CommonSendAnalyticEvents.ExploreButtonClicked(analyticsCategoryName),
        )
        urlOpener.openUrl(confirmUM.txUrl)
    }

    override fun onShareClick() {
        val confirmUM = uiState.value.confirmUM as? ConfirmUM.Success ?: return
        analyticsEventHandler.send(
            CommonSendAnalyticEvents.ShareButtonClicked(analyticsCategoryName),
        )
        shareManager.shareText(confirmUM.txUrl)
    }

    override fun onFailedTxEmailClick(errorMessage: String) {
        saveBlockchainErrorUseCase(
            error = BlockchainErrorInfo(
                errorMessage = errorMessage,
                blockchainId = cryptoCurrency.network.id.value,
                derivationPath = cryptoCurrency.network.derivationPath.value,
                destinationAddress = confirmData.enteredDestination.orEmpty(),
                tokenSymbol = null,
                amount = params.nftAsset.amount.toString(),
                fee = confirmData.fee?.amount?.value?.stripZeroPlainString(),
            ),
        )

        val cardInfo = getCardInfoUseCase(userWallet.scanResponse).getOrNull() ?: return

        modelScope.launch {
            sendFeedbackEmailUseCase(type = FeedbackEmailType.TransactionSendingProblem(cardInfo = cardInfo))
        }
    }

    private fun initialState() {
        val confirmUM = uiState.value.confirmUM
        val feeUM = uiState.value.feeUM

        modelScope.launch {
            val isShowTapHelp = isSendTapHelpEnabledUseCase().getOrElse { false }
            if (confirmUM is ConfirmUM.Empty || feeUM is FeeUM.Empty) {
                _uiState.update {
                    it.copy(
                        confirmUM = NFTSendConfirmInitialStateTransformer(
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
        val destination = destinationUM?.addressTextField?.value ?: return
        val memo = destinationUM?.memoTextField?.value
        val fee = feeSelectorUM?.selectedFee ?: return
        val ownerAddress = cryptoCurrencyStatus.value.networkAddress?.defaultAddress?.value ?: return

        val sdkNFTAsset = NFTSdkAssetConverter.convertBack(params.nftAsset)

        modelScope.launch {
            createNFTTransferTransactionUseCase(
                ownerAddress = ownerAddress,
                nftAsset = sdkNFTAsset.second,
                fee = fee,
                memo = memo,
                destinationAddress = destination,
                userWalletId = userWallet.walletId,
                network = cryptoCurrency.network,
            ).fold(
                ifLeft = { error ->
                    Timber.e(error)
                    _uiState.update(NFTSendConfirmSendingStateTransformer(isSending = false))
                    alertFactory.getGenericErrorState(
                        onFailedTxEmailClick = { onFailedTxEmailClick(error.localizedMessage.orEmpty()) },
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
            userWallet = params.userWallet,
            network = params.cryptoCurrencyStatus.currency.network,
        )

        _uiState.update(NFTSendConfirmSendingStateTransformer(isSending = false))

        result.fold(
            ifLeft = { error ->
                Timber.e(error.toString())
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
                sendBalanceUpdater.scheduleUpdates()
                nftSendAnalyticHelper.nftSendSuccessAnalytics(cryptoCurrency, uiState.value)
            },
        )
    }

    private fun subscribeOnCheckFeeResultUpdates() {
        sendFeeCheckReloadListener.checkReloadResultFlow.onEach { isFeeResultSuccess ->
            if (isFeeResultSuccess) {
                sendIdleTimer = SystemClock.elapsedRealtime()
                _uiState.update(NFTSendConfirmSendingStateTransformer(isSending = true))
                verifyAndSendTransaction()
            } else {
                _uiState.update(NFTSendConfirmSendingStateTransformer(isSending = false))
            }
        }.launchIn(modelScope)
    }

    private fun updateTransactionStatus(txData: TransactionData.Uncompiled) {
        val txUrl = getExplorerTransactionUrlUseCase(
            txHash = txData.hash.orEmpty(),
            networkId = cryptoCurrency.network.id,
        ).getOrElse { "" }
        _uiState.update(NFTSendConfirmSentStateTransformer(txData, txUrl))
    }

    private fun updateConfirmNotifications() {
        modelScope.launch {
            notificationsUpdateTrigger.triggerUpdate(
                data = NotificationData(
                    destinationAddress = confirmData.enteredDestination.orEmpty(),
                    memo = confirmData.enteredMemo,
                    amountValue = BigDecimal.ZERO,
                    reduceAmountBy = BigDecimal.ZERO,
                    isIgnoreReduce = false,
                    fee = confirmData.fee,
                    feeError = confirmData.feeError,
                ),
            )
        }
        _uiState.update {
            it.copy(
                confirmUM = NFTSendConfirmationNotificationsTransformer(
                    feeUM = uiState.value.feeUM,
                    analyticsEventHandler = analyticsEventHandler,
                    cryptoCurrency = cryptoCurrencyStatus.currency,
                    analyticsCategoryName = analyticsCategoryName,
                ).transform(uiState.value.confirmUM),
            )
        }
    }

    private fun configConfirmNavigation() {
        combine(
            flow = uiState,
            flow2 = params.currentRoute,
            transform = { state, route -> state to route },
        ).onEach { (state, _) ->
            val confirmUM = state.confirmUM
            val isReadyToSend = confirmUM is ConfirmUM.Content && !confirmUM.isSending
            params.callback.onResult(
                state.copy(
                    navigationUM = NavigationUM.Content(
                        title = resourceReference(
                            id = R.string.send_summary_title,
                            formatArgs = wrappedList(params.cryptoCurrencyStatus.currency.name),
                        ),
                        subtitle = null,
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
