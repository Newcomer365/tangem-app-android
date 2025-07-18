package com.tangem.tap.domain.walletconnect2.data

import android.app.Application
import arrow.core.flatten
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.data.walletconnect.pair.unsupportedDApps
import com.tangem.domain.walletconnect.WcPairService
import com.tangem.domain.walletconnect.model.WcPairRequest
import com.tangem.domain.walletconnect.model.legacy.Account
import com.tangem.domain.walletconnect.usecase.initialize.WcInitializeUseCase
import com.tangem.features.walletconnect.components.WalletConnectFeatureToggles
import com.tangem.tap.common.analytics.events.WalletConnect
import com.tangem.tap.domain.walletconnect2.app.TangemWcBlockchainHelper
import com.tangem.tap.domain.walletconnect2.domain.LegacyWalletConnectRepository
import com.tangem.tap.domain.walletconnect2.domain.WcJrpcMethods
import com.tangem.tap.domain.walletconnect2.domain.WcJrpcRequestsDeserializer
import com.tangem.tap.domain.walletconnect2.domain.WcRequest
import com.tangem.tap.domain.walletconnect2.domain.models.*
import com.tangem.tap.features.details.redux.walletconnect.WalletConnectAction.OpenSession.SourceType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

internal class DefaultLegacyWalletConnectRepositoryFacade constructor(
    private val stub: LegacyWalletConnectRepositoryStub,
    private val legacy: DefaultLegacyWalletConnectRepository,
    private val walletConnectFeatureToggles: WalletConnectFeatureToggles,
    private val wcInitializeUseCase: WcInitializeUseCase,
    private val wcPairService: WcPairService,
) : LegacyWalletConnectRepository {
    private val isNewWc by lazy {
        walletConnectFeatureToggles.isRedesignedWalletConnectEnabled
    }
    override val events: Flow<WalletConnectEvents> by lazy {
        if (isNewWc) stub.events else legacy.events
    }
    override val activeSessions: Flow<List<WalletConnectSession>> by lazy {
        if (isNewWc) stub.activeSessions else legacy.activeSessions
    }
    override val currentSessions: List<WalletConnectSession> by lazy {
        if (isNewWc) stub.currentSessions else legacy.currentSessions
    }

    override fun init(projectId: String) {
        if (isNewWc) {
            wcInitializeUseCase.init(projectId)
        } else {
            legacy.init(projectId)
        }
    }

    override fun setUserNamespaces(userNamespaces: Map<NetworkNamespace, List<Account>>) {
        if (isNewWc) stub.setUserNamespaces(userNamespaces) else legacy.setUserNamespaces(userNamespaces)
    }

    override fun updateSessions() {
        if (isNewWc) stub.updateSessions() else legacy.updateSessions()
    }

    override fun pair(uri: String, source: SourceType) {
        val src = when (source) {
            SourceType.QR -> WcPairRequest.Source.QR
            SourceType.DEEPLINK -> WcPairRequest.Source.DEEPLINK
            SourceType.CLIPBOARD -> WcPairRequest.Source.CLIPBOARD
            SourceType.ETC -> WcPairRequest.Source.ETC
        }
        if (isNewWc) wcPairService.pair(WcPairRequest(uri, src)) else legacy.pair(uri, source)
    }

    override fun disconnect(topic: String) {
        if (isNewWc) stub.disconnect(topic) else legacy.disconnect(topic)
    }

    override fun approve(userNamespaces: Map<NetworkNamespace, List<Account>>, blockchainNames: List<String>) {
        if (isNewWc) stub.approve(userNamespaces, blockchainNames) else legacy.approve(userNamespaces, blockchainNames)
    }

    override fun reject() {
        if (isNewWc) stub.reject() else legacy.reject()
    }

    override fun sendRequest(requestData: RequestData, result: String) {
        if (isNewWc) stub.sendRequest(requestData, result) else legacy.sendRequest(requestData, result)
    }

    override fun rejectRequest(requestData: RequestData, error: WalletConnectError) {
        if (isNewWc) stub.rejectRequest(requestData, error) else legacy.rejectRequest(requestData, error)
    }

    override fun cancelRequest(topic: String, id: Long, message: String) {
        if (isNewWc) stub.cancelRequest(topic, id, message) else legacy.cancelRequest(topic, id, message)
    }
}

internal class LegacyWalletConnectRepositoryStub : LegacyWalletConnectRepository {
    override val events: Flow<WalletConnectEvents> = emptyFlow()
    override val activeSessions: Flow<List<WalletConnectSession>> = emptyFlow()
    override val currentSessions: List<WalletConnectSession> = listOf()

    override fun init(projectId: String) = Unit

    override fun setUserNamespaces(userNamespaces: Map<NetworkNamespace, List<Account>>) = Unit

    override fun updateSessions() = Unit

    override fun pair(uri: String, source: SourceType) = Unit

    override fun disconnect(topic: String) = Unit

    override fun approve(userNamespaces: Map<NetworkNamespace, List<Account>>, blockchainNames: List<String>) = Unit

    override fun reject() = Unit

    override fun sendRequest(requestData: RequestData, result: String) = Unit

    override fun rejectRequest(requestData: RequestData, error: WalletConnectError) = Unit

    override fun cancelRequest(topic: String, id: Long, message: String) = Unit
}

@Suppress("LargeClass")
internal class DefaultLegacyWalletConnectRepository(
    private val application: Application,
    private val wcRequestDeserializer: WcJrpcRequestsDeserializer,
    private val analyticsHandler: AnalyticsEventHandler,
) : LegacyWalletConnectRepository {

    private var sessionProposal: Wallet.Model.SessionProposal? = null
    private var userNamespaces: Map<NetworkNamespace, List<Account>>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events: MutableSharedFlow<WalletConnectEvents> = MutableSharedFlow()
    override val events: Flow<WalletConnectEvents> = _events

    private val _activeSessions: MutableSharedFlow<List<WalletConnectSession>> = MutableSharedFlow()
    override val activeSessions: Flow<List<WalletConnectSession>> = _activeSessions
    private val blockchainHelper by lazy { TangemWcBlockchainHelper() }

    override var currentSessions: List<WalletConnectSession> = emptyList()
        private set

    /**
     * @param projectId Project ID at https://cloud.walletconnect.com/
     */
    override fun init(projectId: String) {
        val relayUrl = "relay.walletconnect.com"
        val serverUrl = "wss://$relayUrl?projectId=$projectId"
        val connectionType = ConnectionType.AUTOMATIC

        val appMetaData = Core.Model.AppMetaData(
            name = "Tangem",
            description = "Tangem Wallet",
            url = "tangem.com",
            icons = listOf(
                "https://user-images.githubusercontent.com/24321494/124071202-72a00900-da58-11eb-935a-dcdab21de52b.png",
            ),
            redirect = "kotlin-wallet-wc:/request", // Custom Redirect URI
        )

        CoreClient.initialize(
            relayServerUrl = serverUrl,
            connectionType = connectionType,
            application = application,
            metaData = appMetaData,
        ) { error ->
            Timber.e("Error while initializing client: $error")
            scope.launch {
                _events.emit(
                    WalletConnectEvents.SessionApprovalError(
                        WalletConnectError.ExternalApprovalError(error.throwable.message),
                    ),
                )
            }
        }

        WalletKit.initialize(
            Wallet.Params.Init(core = CoreClient),
            onSuccess = {
                val walletDelegate = defineWalletDelegate()
                WalletKit.setWalletDelegate(walletDelegate)
            },
            onError = { error ->
                Timber.e("Error while initializing Web3Wallet: $error")
                scope.launch {
                    _events.emit(
                        WalletConnectEvents.SessionApprovalError(
                            WalletConnectError.ExternalApprovalError(error.throwable.message),
                        ),
                    )
                }
            },
        )
    }

    private fun defineWalletDelegate(): WalletKit.WalletDelegate {
        return object : WalletKit.WalletDelegate {

            @Suppress("LongMethod")
            override fun onSessionProposal(
                sessionProposal: Wallet.Model.SessionProposal,
                verifyContext: Wallet.Model.VerifyContext,
            ) {
                // Triggered when wallet receives the session proposal sent by a Dapp
                Timber.i("sessionProposal: $sessionProposal")
                this@DefaultLegacyWalletConnectRepository.sessionProposal = sessionProposal

                if (sessionProposal.name in unsupportedDApps) {
                    Timber.i("Unsupported DApp")
                    scope.launch {
                        _events.emit(
                            WalletConnectEvents.SessionApprovalError(
                                WalletConnectError.UnsupportedDApp,
                            ),
                        )
                    }
                    return
                }

                val missingNetworks = findMissingNetworks(
                    namespaces = sessionProposal.requiredNamespaces,
                    userNamespaces = this@DefaultLegacyWalletConnectRepository.userNamespaces ?: emptyMap(),
                )

                if (missingNetworks.isNotEmpty()) {
                    Timber.i("Not added blockchains: $missingNetworks")
                    scope.launch {
                        _events.emit(
                            WalletConnectEvents.SessionApprovalError(
                                WalletConnectError.ApprovalErrorMissingNetworks(missingNetworks.toList()),
                            ),
                        )
                    }
                    return
                }

                val optionalMissingNetwork = findMissingNetworks(
                    namespaces = sessionProposal.optionalNamespaces,
                    userNamespaces = this@DefaultLegacyWalletConnectRepository.userNamespaces ?: emptyMap(),
                )

                val optionalWithoutMissingNetworks = removeMissingNetworks(
                    namespaces = sessionProposal.optionalNamespaces,
                    userNamespaces = this@DefaultLegacyWalletConnectRepository.userNamespaces ?: emptyMap(),
                )

                // for cases when optionalNamespaces is not empty but we doesn't support none of them
                if (optionalMissingNetwork.isNotEmpty() &&
                    optionalWithoutMissingNetworks.isEmpty() &&
                    sessionProposal.requiredNamespaces.isEmpty() // if requiredNamespaces is not empty we can connect
                ) {
                    Timber.i("Not added optional blockchains: $optionalMissingNetwork")

                    val unsupportedNetworks = sessionProposal.optionalNamespaces.values
                        .flatMap { it.chains ?: emptyList() }
                        .filter { blockchainHelper.chainIdToNetworkIdOrNull(it) == null }

                    scope.launch {
                        val error = if (unsupportedNetworks.isNotEmpty()) {
                            WalletConnectEvents.SessionApprovalError(
                                WalletConnectError.ApprovalErrorUnsupportedNetwork(unsupportedNetworks),
                            )
                        } else {
                            WalletConnectEvents.SessionApprovalError(
                                WalletConnectError.ApprovalErrorMissingNetworks(optionalMissingNetwork.toList()),
                            )
                        }
                        _events.emit(error)
                    }
                    return
                }

                val requiredChainIds = sessionProposal.requiredNamespaces.values.flatMap { it.chains ?: emptyList() }
                val optionalChainIds = optionalWithoutMissingNetworks.toList()
                val networks = (requiredChainIds + optionalChainIds)
                    .mapNotNull { blockchainHelper.chainIdToFullNameOrNull(it) }
                    .distinct()
                analyticsHandler.send(WalletConnect.DAppConnectionRequested(networks))

                scope.launch {
                    _events.emit(
                        WalletConnectEvents.SessionProposal(
                            sessionProposal.name,
                            sessionProposal.description,
                            sessionProposal.url,
                            sessionProposal.icons,
                            requiredChainIds,
                            optionalChainIds,
                        ),
                    )
                }
            }

            override fun onSessionRequest(
                sessionRequest: Wallet.Model.SessionRequest,
                verifyContext: Wallet.Model.VerifyContext,
            ) {
                // Triggered when a Dapp sends SessionRequest to sign a transaction or a message
                Timber.i("sessionRequest: $sessionRequest")
                val request = wcRequestDeserializer.deserialize(
                    method = sessionRequest.request.method,
                    params = sessionRequest.request.params,
                )
                Timber.i("sessionRequestParsed: $request")

                when (request) {
                    is WcRequest.AddChain -> {
                        // we can send approval automatically, because in WC 2.0 the list of chains is approved when
                        // initial connection is established
                        sendRequest(
                            RequestData(
                                topic = sessionRequest.topic,
                                requestId = sessionRequest.request.id,
                                blockchain = sessionRequest.chainId.toString(),
                                method = WcJrpcMethods.WALLET_ADD_ETHEREUM_CHAIN.code,
                            ),
                            result = "",
                        )
                    }
                    else -> {
                        val event = WalletConnect.SignatureRequestReceived(
                            WalletConnect.RequestHandledParams(
                                dAppName = sessionRequest.peerMetaData?.name ?: "",
                                dAppUrl = sessionRequest.peerMetaData?.url ?: "",
                                methodName = sessionRequest.request.method,
                                blockchain = sessionRequest.chainId
                                    ?.let { blockchainHelper.chainIdToNetworkIdOrNull(it) } ?: "",
                            ),
                        )
                        analyticsHandler.send(event)
                        scope.launch {
                            _events.emit(
                                WalletConnectEvents.SessionRequest(
                                    request = request,
                                    chainId = sessionRequest.chainId,
                                    topic = sessionRequest.topic,
                                    id = sessionRequest.request.id,
                                    metaUrl = sessionRequest.peerMetaData?.url ?: "",
                                    metaName = sessionRequest.peerMetaData?.name ?: "",
                                    method = sessionRequest.request.method,
                                ),
                            )
                        }
                    }
                }
            }

            override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
                // Triggered when the session is deleted by the peer
                if (sessionDelete is Wallet.Model.SessionDelete.Success) {
                    scope.launch {
                        _events.emit(WalletConnectEvents.SessionDeleted(sessionDelete.topic))
                        updateSessionsInternal().join()
                    }
                }
                Timber.i("onSessionDelete: $sessionDelete")
            }

            override fun onSessionExtend(session: Wallet.Model.Session) {
                Timber.i("onSessionExtend: $session")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
                // Triggered when wallet receives the session settlement response from Dapp
                Timber.i("onSessionSettleResponse: $settleSessionResponse")
                if (settleSessionResponse is Wallet.Model.SettledSessionResponse.Result) {
                    scope.launch {
                        _events.emit(
                            WalletConnectEvents.SessionApprovalSuccess(
                                topic = settleSessionResponse.session.topic,
                                accounts = userNamespaces?.flatMap { it.value } ?: emptyList(),
                            ),
                        )
                        updateSessionsInternal().join()
                    }
                }
            }

            override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
                // Triggered when wallet receives the session update response from Dapp
                Timber.i("onSessionUpdateResponse: $sessionUpdateResponse")
                updateSessionsInternal()
            }

            override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
                // Triggered whenever the connection state is changed
                Timber.i("onConnectionStateChange: $state")
                if (state.isAvailable) updateSessionsInternal()
            }

            override fun onError(error: Wallet.Model.Error) {
                // Triggered whenever there is an issue inside the SDK
                Timber.i("onError: $error")
            }
        }
    }

    override fun setUserNamespaces(userNamespaces: Map<NetworkNamespace, List<Account>>) {
        this.userNamespaces = userNamespaces
    }

    override fun pair(uri: String, source: SourceType) {
        analyticsHandler.send(WalletConnect.NewSessionInitiated(source = source))
        WalletKit.pair(
            params = Wallet.Params.Pair(uri),
            onSuccess = {
                Timber.i("Paired successfully: $it")
            },
            onError = {
                Timber.e("Error while pairing: $it")
                analyticsHandler.send(WalletConnect.SessionFailed)
                scope.launch {
                    _events.emit(
                        WalletConnectEvents.PairConnectError(it.throwable),
                    )
                }
            },
        )
    }

    override fun approve(userNamespaces: Map<NetworkNamespace, List<Account>>, blockchainNames: List<String>) {
        val sessionProposal: Wallet.Model.SessionProposal = requireNotNull(this.sessionProposal)

        val userChains = userNamespaces.flatMap { namespace ->
            namespace.value.map { it.chainId to "${it.chainId}:${it.walletAddress}" }
        }.groupBy { pair -> pair.first }
            .mapValues { entry -> entry.value.map { pair -> pair.second }.toSet() }

        val preparedRequiredNamespaces = sessionProposal.requiredNamespaces
            .map { requiredNamespace ->
                val accountsRequired = requiredNamespace.value.chains
                    ?.mapNotNull { chain -> userChains[chain] }
                    ?.flatten() ?: emptyList()
                val optionalNamespace = sessionProposal.optionalNamespaces[requiredNamespace.key]
                val accountsOptional = optionalNamespace?.chains
                    ?.mapNotNull { chain -> userChains[chain] }
                    ?.flatten() ?: emptyList()

                val methods = (requiredNamespace.value.methods + (optionalNamespace?.methods ?: emptyList()))
                    .distinct()
                requiredNamespace.key to Wallet.Model.Namespace.Session(
                    accounts = (accountsRequired + accountsOptional).distinct(),
                    methods = methods,
                    events = requiredNamespace.value.events,
                )
            }.toMap()

        val sessionApproval = Wallet.Params.SessionApprove(
            proposerPublicKey = sessionProposal.proposerPublicKey,
            namespaces = preparedRequiredNamespaces.ifEmpty {
                sessionProposal.createPreparedOptionalNamespaces(userChains)
            },
        )

        Timber.i("Session approval is prepared for sending: $sessionApproval")

        WalletKit.approveSession(
            params = sessionApproval,
            onSuccess = {
                Timber.i("Approved successfully: $it")
                analyticsHandler.send(
                    WalletConnect.DAppConnected(
                        dAppName = sessionProposal.name,
                        dAppUrl = sessionProposal.url,
                        blockchainNames = blockchainNames,
                    ),
                )
            },
            onError = {
                Timber.e("Error while approving: $it")
                analyticsHandler.send(
                    WalletConnect.DAppConnectionFailed(
                        dAppName = sessionProposal.name,
                        dAppUrl = sessionProposal.url,
                        blockchainNames = blockchainNames,
                    ),
                )
                scope.launch {
                    _events.emit(
                        WalletConnectEvents.SessionApprovalError(
                            WalletConnectError.ExternalApprovalError(it.throwable.message),
                        ),
                    )
                }
            },
        )
    }

    private fun Wallet.Model.SessionProposal.createPreparedOptionalNamespaces(
        userChains: Map<String, Set<String>>,
    ): Map<String, Wallet.Model.Namespace.Session> {
        return optionalNamespaces
            .map { optionalNamespace ->
                val accountsOptional = optionalNamespace.value.chains
                    ?.mapNotNull { chain -> userChains[chain] }
                    ?.flatten() ?: emptyList()

                val methods = optionalNamespace.value.methods
                optionalNamespace.key to Wallet.Model.Namespace.Session(
                    accounts = accountsOptional.distinct(),
                    methods = methods,
                    chains = userChains.keys
                        .filter { it.startsWith(optionalNamespace.key) }
                        .toList(),
                    events = optionalNamespace.value.events,
                )
            }
            .toMap()
    }

    override fun sendRequest(requestData: RequestData, result: String) {
        val session = currentSessions.find { it.topic == requestData.topic }
        // Add Ethereum Chain method is processed without user input, skip logging it
        if (requestData.method != WcJrpcMethods.WALLET_ADD_ETHEREUM_CHAIN.code) {
            analyticsHandler.send(
                WalletConnect.SignatureRequestHandled(
                    WalletConnect.RequestHandledParams(
                        dAppName = session?.name ?: "",
                        dAppUrl = session?.url ?: "",
                        methodName = requestData.method,
                        blockchain = requestData.blockchain,
                    ),
                ),
            )
        }

        val params = Wallet.Params.SessionRequestResponse(
            sessionTopic = requestData.topic,
            jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                id = requestData.requestId,
                result = result,
            ),
        )
        Timber.i("Session request response: $params")

        WalletKit.respondSessionRequest(
            params = params,
            onSuccess = { response ->
                Timber.i("Session request responded successfully: $response")
            },
            onError = { error ->
                Timber.e(error.throwable, "Error while responging session request")

                val handledParams = WalletConnect.RequestHandledParams(
                    dAppName = session?.name ?: "",
                    dAppUrl = session?.url ?: "",
                    methodName = requestData.method,
                    blockchain = requestData.blockchain,
                    errorCode = WalletConnectError.ValidationError.error,
                    errorDescription = error.throwable.message,
                )
                analyticsHandler.send(WalletConnect.SignatureRequestFailed(handledParams))
            },
        )
    }

    override fun rejectRequest(requestData: RequestData, error: WalletConnectError) {
        val session = currentSessions.find { it.topic == requestData.topic }

        analyticsHandler.send(
            WalletConnect.SignatureRequestFailed(
                WalletConnect.RequestHandledParams(
                    dAppName = session?.name ?: "",
                    dAppUrl = session?.url ?: "",
                    methodName = requestData.method,
                    blockchain = requestData.blockchain,
                    errorCode = error.toString(),
                ),
            ),
        )

        cancelRequest(requestData.topic, requestData.requestId, error.error)
    }

    override fun cancelRequest(topic: String, id: Long, message: String) {
        WalletKit.respondSessionRequest(
            params = Wallet.Params.SessionRequestResponse(
                sessionTopic = topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                    id = id,
                    code = 0,
                    message = message,
                ),
            ),
            onSuccess = {},
            onError = {},
        )
    }

    override fun reject() {
        WalletKit.rejectSession(
            params = Wallet.Params.SessionReject(
                proposerPublicKey = sessionProposal?.proposerPublicKey ?: "",
                reason = "",
            ),
            onSuccess = {
                Timber.i("Rejected successfully: $it")
            },
            onError = {
                Timber.e("Error while rejecting: $it")
            },
        )
    }

    override fun disconnect(topic: String) {
        val session = currentSessions.find { it.topic == topic }
        WalletKit.disconnectSession(
            params = Wallet.Params.SessionDisconnect(topic),
            onSuccess = {
                analyticsHandler.send(
                    WalletConnect.SessionDisconnected(
                        dAppName = session?.name ?: "",
                        dAppUrl = session?.url ?: "",
                    ),
                )
                updateSessionsInternal()
                Timber.i("Disconnected successfully: $it")
            },
            onError = {
                Timber.e("Error while disconnecting: $it")
            },
        )
    }

    fun send(topic: String, id: Long, data: String) {
        WalletKit.respondSessionRequest(
            params = Wallet.Params.SessionRequestResponse(
                sessionTopic = topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    id = id,
                    result = data,
                ),
            ),
            onError = {},
            onSuccess = {},
        )
    }

    override fun updateSessions() {
        updateSessionsInternal()
    }

    private fun updateSessionsInternal(): Job = scope.launch {
        val availableSessions = WalletKit.getListOfActiveSessions()
            .map {
                WalletConnectSession(
                    topic = it.topic,
                    icon = it.metaData?.icons?.firstOrNull(),
                    name = it.metaData?.name,
                    url = it.metaData?.url,
                )
            }
        Timber.i("Available sessions: $availableSessions")
        currentSessions = availableSessions
        _activeSessions.emit(availableSessions)
    }

    private fun findMissingNetworks(
        namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
        userNamespaces: Map<NetworkNamespace, List<Account>>,
    ): Collection<String> {
        val requiredChains = namespaces.values.flatMap { it.chains ?: emptyList() }
        val userChains = userNamespaces.flatMap { it.value.map { account -> account.chainId } }
        return requiredChains.subtract(userChains.toSet())
    }

    private fun removeMissingNetworks(
        namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
        userNamespaces: Map<NetworkNamespace, List<Account>>,
    ): Collection<String> {
        val wcProvidedChains = namespaces.values.flatMap { it.chains ?: emptyList() }
        val userChains = userNamespaces.flatMap { it.value.map { account -> account.chainId } }
        return wcProvidedChains.intersect(userChains.toSet())
    }
}
