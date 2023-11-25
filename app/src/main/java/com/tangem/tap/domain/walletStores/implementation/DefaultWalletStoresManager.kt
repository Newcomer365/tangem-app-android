package com.tangem.tap.domain.walletStores.implementation

import com.tangem.blockchain.common.WalletManager
import com.tangem.blockchain.common.address.AddressType
import com.tangem.common.*
import com.tangem.domain.common.util.derivationStyleProvider
import com.tangem.domain.wallets.legacy.WalletManagersRepository
import com.tangem.domain.wallets.models.UserWallet
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.tap.common.entities.FiatCurrency
import com.tangem.tap.domain.model.WalletStoreModel
import com.tangem.tap.domain.model.builders.WalletStoreBuilder
import com.tangem.tap.domain.tokens.UserTokensRepository
import com.tangem.tap.domain.walletStores.WalletStoresError
import com.tangem.tap.domain.walletStores.WalletStoresManager
import com.tangem.tap.domain.walletStores.repository.WalletAmountsRepository
import com.tangem.tap.domain.walletStores.repository.WalletStoresRepository
import com.tangem.tap.domain.walletStores.repository.implementation.utils.updateSelectedAddress
import com.tangem.tap.features.wallet.models.Currency
import com.tangem.tap.features.wallet.models.toBlockchainNetworks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class DefaultWalletStoresManager(
    private val userTokensRepository: UserTokensRepository,
    private val walletStoresRepository: WalletStoresRepository,
    private val walletAmountsRepository: WalletAmountsRepository,
    private val walletManagersRepository: WalletManagersRepository,
    private val appCurrencyProvider: () -> FiatCurrency,
) : WalletStoresManager {
    private val state = MutableStateFlow(State())

    override fun getAll(): Flow<Map<UserWalletId, List<WalletStoreModel>>> {
        return walletStoresRepository.getAll()
    }

    override fun get(userWalletId: UserWalletId): Flow<List<WalletStoreModel>> {
        return walletStoresRepository.get(userWalletId)
            .distinctUntilChanged()
    }

    override suspend fun getSync(userWalletId: UserWalletId): List<WalletStoreModel> {
        return walletStoresRepository.getSync(userWalletId)
    }

    override suspend fun delete(userWalletsIds: List<UserWalletId>): CompletionResult<Unit> {
        return walletStoresRepository.delete(userWalletsIds)
            .flatMap { walletManagersRepository.delete(userWalletsIds) }
    }

    override suspend fun clear(): CompletionResult<Unit> {
        return walletStoresRepository.clear()
    }

    override suspend fun fetch(userWallets: List<UserWallet>, refresh: Boolean): CompletionResult<Unit> =
        withContext(Dispatchers.Default) {
            val fiatCurrency = appCurrencyProvider.invoke()
            val isFiatCurrencyChanged = state.value.fiatCurrency != fiatCurrency

            state.update { prevState ->
                prevState.copy(
                    fiatCurrency = fiatCurrency,
                )
            }

            userWallets
                .mapNotNull { userWallet ->
                    val hasNotWalletStoresForUserWallet = !walletStoresRepository.contains(userWallet.walletId)
                    if (refresh || hasNotWalletStoresForUserWallet || isFiatCurrencyChanged) {
                        fetchWalletsIfNeeded(userWallet)
                    } else {
                        null
                    }
                }
                .fold(arrayListOf<UserWallet>()) { acc, data ->
                    acc.apply { add(data) }
                }
                .flatMap {
                    walletAmountsRepository.updateAmountsForUserWallets(it, fiatCurrency)
                }
        }

    override suspend fun fetch(userWallet: UserWallet, refresh: Boolean): CompletionResult<Unit> {
        return fetch(listOf(userWallet), refresh)
    }

    override suspend fun updateAmounts(userWallets: List<UserWallet>): CompletionResult<Unit> {
        val fiatCurrency = appCurrencyProvider.invoke()

        return walletAmountsRepository.updateAmountsForUserWallets(userWallets, fiatCurrency)
            .doOnSuccess {
                state.update { prevState ->
                    prevState.copy(
                        fiatCurrency = fiatCurrency,
                    )
                }
            }
    }

    override suspend fun updateSelectedAddress(
        userWalletId: UserWalletId,
        currency: Currency,
        addressType: AddressType,
    ): CompletionResult<Unit> {
        return walletStoresRepository.update(userWalletId) { walletStores ->
            walletStores
                .firstOrNull {
                    it.blockchain == currency.blockchain &&
                        it.derivationPath?.rawPath == currency.derivationPath
                }
                ?.updateSelectedAddress(currency, addressType)
        }
    }

    private suspend fun fetchWalletsIfNeeded(userWallet: UserWallet): CompletionResult<UserWallet> {
        return if (userWallet.isMultiCurrency) {
            fetchMultiWallets(userWallet)
        } else {
            fetchSingleWallet(userWallet)
        }
            .map { userWallet }
    }

    private suspend fun fetchMultiWallets(userWallet: UserWallet): CompletionResult<Unit> {
        val scanResponse = userWallet.scanResponse
        val derivationStyle = scanResponse.derivationStyleProvider.getDerivationStyle()
        val userTokens = withContext(Dispatchers.IO) {
            userTokensRepository.getUserTokens(scanResponse.card, derivationStyle)
        }
        val userWalletId = userWallet.walletId

        return withContext(Dispatchers.Default) {
            userTokens.toBlockchainNetworks()
                .also { blockchainNetworks ->
                    walletStoresRepository.deleteDifference(
                        userWalletId = userWalletId,
                        currentBlockchains = blockchainNetworks.map {
                            Currency.Blockchain(it.blockchain, it.derivationPath)
                        },
                    )
                }
                .map { blockchainNetwork ->
                    val storeWalletStore: suspend (WalletManager?) -> CompletionResult<Unit> =
                        { walletManager ->
                            walletStoresRepository.storeOrUpdate(
                                userWalletId = userWalletId,
                                walletStore = WalletStoreBuilder(userWallet, blockchainNetwork)
                                    .walletManager(walletManager)
                                    .build(),
                            )
                        }

                    walletManagersRepository.findOrMakeMultiCurrencyWalletManager(
                        userWallet = userWallet,
                        blockchainNetwork = blockchainNetwork,
                    )
                        .flatMap { walletManager ->
                            storeWalletStore(walletManager)
                        }
                        .flatMapOnFailure { error ->
                            when (error) {
                                is WalletStoresError.WalletManagerNotCreated,
                                is WalletStoresError.UpdateWalletManagerTokensError,
                                -> storeWalletStore(null)
                                else -> CompletionResult.Failure(error)
                            }
                        }
                }
                .fold()
        }
    }

    private suspend fun fetchSingleWallet(userWallet: UserWallet): CompletionResult<Unit> {
        return walletManagersRepository.findOrMakeSingleCurrencyWalletManager(
            userWallet = userWallet,
        )
            .flatMap { walletManager ->
                val userWalletId = userWallet.walletId
                walletStoresRepository.storeOrUpdate(
                    userWalletId = userWalletId,
                    walletStore = WalletStoreBuilder(userWallet, walletManager)
                        .build(),
                )
            }
            .flatMapOnFailure { error ->
                when (error) {
                    is WalletStoresError.WalletManagerNotCreated,
                    is WalletStoresError.UpdateWalletManagerTokensError,
                    -> CompletionResult.Success(Unit)
                    else -> CompletionResult.Failure(error)
                }
            }
    }

    internal data class State(
        val fiatCurrency: FiatCurrency? = null,
    )
}