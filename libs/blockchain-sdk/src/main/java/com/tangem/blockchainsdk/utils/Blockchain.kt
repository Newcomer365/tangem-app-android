package com.tangem.blockchainsdk.utils

import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.ReserveAmountProvider
import com.tangem.blockchain.common.Token
import com.tangem.blockchain.common.WalletManager
import java.math.BigDecimal

@Suppress("ComplexMethod", "LongMethod")
fun Blockchain.Companion.fromNetworkId(networkId: String): Blockchain? {
    return when (networkId) {
        "alephium" -> Blockchain.Alephium
        "alephium/test" -> Blockchain.AlephiumTestnet
        "arbitrum-one" -> Blockchain.Arbitrum
        "arbitrum-one/test" -> Blockchain.ArbitrumTestnet
        "avalanche", "avalanche-2" -> Blockchain.Avalanche
        "avalanche/test", "avalanche-2/test" -> Blockchain.AvalancheTestnet
        "binancecoin" -> Blockchain.Binance
        "binancecoin/test" -> Blockchain.BinanceTestnet
        "binance-smart-chain" -> Blockchain.BSC
        "binance-smart-chain/test" -> Blockchain.BSCTestnet
        "ethereum" -> Blockchain.Ethereum
        "ethereum/test" -> Blockchain.EthereumTestnet
        "ethereum-classic" -> Blockchain.EthereumClassic
        "ethereum-classic/test" -> Blockchain.EthereumClassicTestnet
        "polygon-pos", "matic-network" -> Blockchain.Polygon
        "polygon-pos/test", "matic-network/test" -> Blockchain.PolygonTestnet
        "solana" -> Blockchain.Solana
        "solana/test" -> Blockchain.SolanaTestnet
        "fantom" -> Blockchain.Fantom
        "fantom/test" -> Blockchain.FantomTestnet
        "bitcoin" -> Blockchain.Bitcoin
        "bitcoin/test" -> Blockchain.BitcoinTestnet
        "bitcoin-cash" -> Blockchain.BitcoinCash
        "bitcoin-cash/test" -> Blockchain.BitcoinCashTestnet
        "cardano" -> Blockchain.Cardano
        "dogecoin" -> Blockchain.Dogecoin
        "ducatus" -> Blockchain.Ducatus
        "litecoin" -> Blockchain.Litecoin
        "rootstock" -> Blockchain.RSK
        "stellar" -> Blockchain.Stellar
        "stellar/test" -> Blockchain.StellarTestnet
        "tezos" -> Blockchain.Tezos
        "tron" -> Blockchain.Tron
        "tron/test" -> Blockchain.TronTestnet
        "xrp", "ripple" -> Blockchain.XRP
        "xdai" -> Blockchain.Gnosis
        "ethereum-pow-iou" -> Blockchain.EthereumPow
        "ethereum-pow-iou/test" -> Blockchain.EthereumPowTestnet
        "ethereumfair", "dischain" -> Blockchain.Dischain // for old client compatibility
        "polkadot" -> Blockchain.Polkadot
        "polkadot/test" -> Blockchain.PolkadotTestnet
        "kusama" -> Blockchain.Kusama
        "optimistic-ethereum" -> Blockchain.Optimism
        "optimistic-ethereum/test" -> Blockchain.OptimismTestnet
        "dash" -> Blockchain.Dash
        "kaspa" -> Blockchain.Kaspa
        "the-open-network" -> Blockchain.TON
        "the-open-network/test" -> Blockchain.TONTestnet
        "kava" -> Blockchain.Kava
        "kava/test" -> Blockchain.KavaTestnet
        "ravencoin" -> Blockchain.Ravencoin
        "ravencoin/test" -> Blockchain.RavencoinTestnet
        "cosmos" -> Blockchain.Cosmos
        "cosmos/test" -> Blockchain.CosmosTestnet
        "terra" -> Blockchain.TerraV1
        "terra-2" -> Blockchain.TerraV2
        "cronos" -> Blockchain.Cronos
        "telos" -> Blockchain.Telos
        "telos/test" -> Blockchain.TelosTestnet
        "aleph-zero" -> Blockchain.AlephZero
        "aleph-zero/test" -> Blockchain.AlephZeroTestnet
        "octaspace" -> Blockchain.OctaSpace
        "octaspace/test" -> Blockchain.OctaSpaceTestnet
        "chia" -> Blockchain.Chia
        "chia/test" -> Blockchain.ChiaTestnet
        "near-protocol" -> Blockchain.Near
        "near-protocol/test" -> Blockchain.NearTestnet
        "decimal" -> Blockchain.Decimal
        "decimal/test" -> Blockchain.DecimalTestnet
        "xdc-network" -> Blockchain.XDC
        "xdc-network/test" -> Blockchain.XDCTestnet
        "vechain" -> Blockchain.VeChain
        "vechain/test" -> Blockchain.VeChainTestnet
        "aptos" -> Blockchain.Aptos
        "aptos/test" -> Blockchain.AptosTestnet
        "playa3ull-games" -> Blockchain.Playa3ull
        "shibarium" -> Blockchain.Shibarium
        "shibarium/test" -> Blockchain.ShibariumTestnet
        "algorand" -> Blockchain.Algorand
        "algorand/test" -> Blockchain.AlgorandTestnet
        "hedera-hashgraph" -> Blockchain.Hedera
        "hedera-hashgraph/test" -> Blockchain.HederaTestnet
        "aurora" -> Blockchain.Aurora
        "aurora/test" -> Blockchain.AuroraTestnet
        "areon-network" -> Blockchain.Areon
        "areon-network/test" -> Blockchain.AreonTestnet
        "pulsechain" -> Blockchain.PulseChain
        "pulsechain/test" -> Blockchain.PulseChainTestnet
        "zksync" -> Blockchain.ZkSyncEra
        "zksync/test" -> Blockchain.ZkSyncEraTestnet
        "moonbeam" -> Blockchain.Moonbeam
        "moonbeam/test" -> Blockchain.MoonbeamTestnet
        "manta-pacific" -> Blockchain.Manta
        "manta-pacific/test" -> Blockchain.MantaTestnet
        "polygon-zkevm" -> Blockchain.PolygonZkEVM
        "polygon-zkevm/test" -> Blockchain.PolygonZkEVMTestnet
        "nexa" -> Blockchain.Nexa // FIXME
        "nexa/test" -> Blockchain.NexaTestnet // FIXME
        "radiant" -> Blockchain.Radiant
        "fact0rn" -> Blockchain.Fact0rn
        "moonriver" -> Blockchain.Moonriver
        "moonriver/test" -> Blockchain.MoonriverTestnet
        "mantle" -> Blockchain.Mantle
        "mantle/test" -> Blockchain.MantleTestnet
        "flare-network" -> Blockchain.Flare
        "flare-network/test" -> Blockchain.FlareTestnet
        "taraxa" -> Blockchain.Taraxa
        "taraxa/test" -> Blockchain.TaraxaTestnet
        "base" -> Blockchain.Base
        "base/test" -> Blockchain.BaseTestnet
        "koinos" -> Blockchain.Koinos
        "koinos/test" -> Blockchain.KoinosTestnet
        "joystream" -> Blockchain.Joystream
        "bittensor" -> Blockchain.Bittensor
        "filecoin" -> Blockchain.Filecoin
        "blast" -> Blockchain.Blast
        "blast/test" -> Blockchain.BlastTestnet
        "cyber" -> Blockchain.Cyber
        "cyber/test" -> Blockchain.CyberTestnet
        "sei-network" -> Blockchain.Sei
        "sei-network/test" -> Blockchain.SeiTestnet
        "internet-computer" -> Blockchain.InternetComputer
        "sui" -> Blockchain.Sui
        "sui/test" -> Blockchain.SuiTestnet
        "energy-web-chain" -> Blockchain.EnergyWebChain
        "energy-web-chain/test" -> Blockchain.EnergyWebChainTestnet
        "energy-web-x" -> Blockchain.EnergyWebX
        "energy-web-x/test" -> Blockchain.EnergyWebXTestnet
        "add_later" -> Blockchain.Casper
        "add_later_test" -> Blockchain.CasperTestnet
        "core" -> Blockchain.Core
        "core/test" -> Blockchain.CoreTestnet
        "casper-network" -> Blockchain.Casper
        "casper-network/test" -> Blockchain.CasperTestnet
        "chiliz" -> Blockchain.Chiliz
        "chiliz/test" -> Blockchain.ChilizTestnet
        "vanar-chain" -> Blockchain.VanarChain
        "vanar-chain/test" -> Blockchain.VanarChainTestnet
        "xodex" -> Blockchain.Xodex
        "canxium" -> Blockchain.Canxium
        "clore-ai" -> Blockchain.Clore
        "dione" -> Blockchain.OdysseyChain
        "dione/test" -> Blockchain.OdysseyChainTestnet
        "bitrock" -> Blockchain.Bitrock
        "bitrock/test" -> Blockchain.BitrockTestnet
        "sonic" -> Blockchain.Sonic
        "sonic/test" -> Blockchain.SonicTestnet
        "apechain" -> Blockchain.ApeChain
        "apechain/test" -> Blockchain.ApeChainTestnet
        "kaspa/test" -> Blockchain.KaspaTestnet
        "scroll" -> Blockchain.Scroll
        "scroll/test" -> Blockchain.ScrollTestnet
        "zklink" -> Blockchain.ZkLinkNova
        "zklink/test" -> Blockchain.ZkLinkNovaTestnet
        "pepecoin" -> Blockchain.Pepecoin
        "pepecoin/test" -> Blockchain.PepecoinTestnet
        else -> null
    }
}

@Suppress("ComplexMethod", "LongMethod")
fun Blockchain.toNetworkId(): String {
    return when (this) {
        Blockchain.Unknown -> "unknown"
        Blockchain.Alephium -> "alephium"
        Blockchain.AlephiumTestnet -> "alephium/test"
        Blockchain.Arbitrum -> "arbitrum-one"
        Blockchain.ArbitrumTestnet -> "arbitrum-one/test"
        Blockchain.Avalanche -> "avalanche"
        Blockchain.AvalancheTestnet -> "avalanche/test"
        Blockchain.Binance -> "binancecoin"
        Blockchain.BinanceTestnet -> "binancecoin/test"
        Blockchain.BSC -> "binance-smart-chain"
        Blockchain.BSCTestnet -> "binance-smart-chain/test"
        Blockchain.Bitcoin -> "bitcoin"
        Blockchain.BitcoinTestnet -> "bitcoin/test"
        Blockchain.BitcoinCash -> "bitcoin-cash"
        Blockchain.BitcoinCashTestnet -> "bitcoin-cash/test"
        Blockchain.Cardano -> "cardano"
        Blockchain.Dogecoin -> "dogecoin"
        Blockchain.Ducatus -> "ducatus"
        Blockchain.Ethereum -> "ethereum"
        Blockchain.EthereumTestnet -> "ethereum/test"
        Blockchain.EthereumClassic -> "ethereum-classic"
        Blockchain.EthereumClassicTestnet -> "ethereum-classic/test"
        Blockchain.Fantom -> "fantom"
        Blockchain.FantomTestnet -> "fantom/test"
        Blockchain.Litecoin -> "litecoin"
        Blockchain.Polygon -> "polygon-pos"
        Blockchain.PolygonTestnet -> "polygon-pos/test"
        Blockchain.RSK -> "rootstock"
        Blockchain.Stellar -> "stellar"
        Blockchain.StellarTestnet -> "stellar/test"
        Blockchain.Solana -> "solana"
        Blockchain.SolanaTestnet -> "solana/test"
        Blockchain.Tezos -> "tezos"
        Blockchain.XRP -> "xrp"
        Blockchain.Tron -> "tron"
        Blockchain.TronTestnet -> "tron/test"
        Blockchain.Gnosis -> "xdai"
        Blockchain.EthereumPow -> "ethereum-pow-iou"
        Blockchain.EthereumPowTestnet -> "ethereum-pow-iou/test"
        Blockchain.Dischain -> "ethereumfair" // for backend compatibility
        Blockchain.Polkadot -> "polkadot"
        Blockchain.PolkadotTestnet -> "polkadot/test"
        Blockchain.Kusama -> "kusama"
        Blockchain.Optimism -> "optimistic-ethereum"
        Blockchain.OptimismTestnet -> "optimistic-ethereum/test"
        Blockchain.Dash -> "dash"
        Blockchain.Kaspa -> "kaspa"
        Blockchain.KaspaTestnet -> "kaspa/test"
        Blockchain.TON -> "the-open-network"
        Blockchain.TONTestnet -> "the-open-network/test"
        Blockchain.Kava -> "kava"
        Blockchain.KavaTestnet -> "kava/test"
        Blockchain.Ravencoin -> "ravencoin"
        Blockchain.RavencoinTestnet -> "ravencoin/test"
        Blockchain.Cosmos -> "cosmos"
        Blockchain.CosmosTestnet -> "cosmos/test"
        Blockchain.TerraV1 -> "terra"
        Blockchain.TerraV2 -> "terra-2"
        Blockchain.Cronos -> "cronos"
        Blockchain.Telos -> "telos"
        Blockchain.TelosTestnet -> "telos/test"
        Blockchain.AlephZero -> "aleph-zero"
        Blockchain.AlephZeroTestnet -> "aleph-zero/test"
        Blockchain.OctaSpace -> "octaspace"
        Blockchain.OctaSpaceTestnet -> "octaspace/test"
        Blockchain.Chia -> "chia"
        Blockchain.ChiaTestnet -> "chia/test"
        Blockchain.Near -> "near-protocol"
        Blockchain.NearTestnet -> "near-protocol/test"
        Blockchain.Decimal -> "decimal"
        Blockchain.DecimalTestnet -> "decimal/test"
        Blockchain.XDC -> "xdc-network"
        Blockchain.XDCTestnet -> "xdc-network/test"
        Blockchain.VeChain -> "vechain"
        Blockchain.VeChainTestnet -> "vechain/test"
        Blockchain.Aptos -> "aptos"
        Blockchain.AptosTestnet -> "aptos/test"
        Blockchain.Playa3ull -> "playa3ull-games"
        Blockchain.Shibarium -> "shibarium"
        Blockchain.ShibariumTestnet -> "shibarium/test"
        Blockchain.Algorand -> "algorand"
        Blockchain.AlgorandTestnet -> "algorand/test"
        Blockchain.Hedera -> "hedera-hashgraph"
        Blockchain.HederaTestnet -> "hedera-hashgraph/test"
        Blockchain.Aurora -> "aurora"
        Blockchain.AuroraTestnet -> "aurora/test"
        Blockchain.Areon -> "areon-network"
        Blockchain.AreonTestnet -> "areon-network/test"
        Blockchain.PulseChain -> "pulsechain"
        Blockchain.PulseChainTestnet -> "pulsechain/test"
        Blockchain.ZkSyncEra -> "zksync"
        Blockchain.ZkSyncEraTestnet -> "zksync/test"
        Blockchain.Moonbeam -> "moonbeam"
        Blockchain.MoonbeamTestnet -> "moonbeam/test"
        Blockchain.Manta -> "manta-pacific"
        Blockchain.MantaTestnet -> "manta-pacific/test"
        Blockchain.PolygonZkEVM -> "polygon-zkevm"
        Blockchain.PolygonZkEVMTestnet -> "polygon-zkevm/test"
        Blockchain.Nexa -> "nexa" // FIXME
        Blockchain.NexaTestnet -> "nexa/test" // FIXME
        Blockchain.Radiant -> "radiant"
        Blockchain.Fact0rn -> "fact0rn"
        Blockchain.Moonriver -> "moonriver"
        Blockchain.MoonriverTestnet -> "moonriver/test"
        Blockchain.Mantle -> "mantle"
        Blockchain.MantleTestnet -> "mantle/test"
        Blockchain.Flare -> "flare-network"
        Blockchain.FlareTestnet -> "flare-network/test"
        Blockchain.Taraxa -> "taraxa"
        Blockchain.TaraxaTestnet -> "taraxa/test"
        Blockchain.Base -> "base"
        Blockchain.BaseTestnet -> "base/test"
        Blockchain.Koinos -> "koinos"
        Blockchain.KoinosTestnet -> "koinos/test"
        Blockchain.Joystream -> "joystream"
        Blockchain.Bittensor -> "bittensor"
        Blockchain.Filecoin -> "filecoin"
        Blockchain.Blast -> "blast"
        Blockchain.BlastTestnet -> "blast/test"
        Blockchain.Cyber -> "cyber"
        Blockchain.CyberTestnet -> "cyber/test"
        Blockchain.Sei -> "sei-network"
        Blockchain.SeiTestnet -> "sei-network/test"
        Blockchain.InternetComputer -> "internet-computer"
        Blockchain.Sui -> "sui"
        Blockchain.SuiTestnet -> "sui/test"
        Blockchain.EnergyWebChain -> "energy-web-chain"
        Blockchain.EnergyWebChainTestnet -> "energy-web-chain/test"
        Blockchain.EnergyWebX -> "energy-web-x"
        Blockchain.EnergyWebXTestnet -> "energy-web-x/test"
        Blockchain.Casper -> "casper-network"
        Blockchain.CasperTestnet -> "casper-network/test"
        Blockchain.Core -> "core"
        Blockchain.CoreTestnet -> "core/test"
        Blockchain.Chiliz -> "chiliz"
        Blockchain.ChilizTestnet -> "chiliz/test"
        Blockchain.VanarChain -> "vanar-chain"
        Blockchain.VanarChainTestnet -> "vanar-chain/test"
        Blockchain.Xodex -> "xodex"
        Blockchain.Canxium -> "canxium"
        Blockchain.Clore -> "clore-ai"
        Blockchain.OdysseyChain -> "dione"
        Blockchain.OdysseyChainTestnet -> "dione/test"
        Blockchain.Bitrock -> "bitrock"
        Blockchain.BitrockTestnet -> "bitrock/test"
        Blockchain.Sonic -> "sonic"
        Blockchain.SonicTestnet -> "sonic/test"
        Blockchain.ApeChain -> "apechain"
        Blockchain.ApeChainTestnet -> "apechain/test"
        Blockchain.Scroll -> "scroll"
        Blockchain.ScrollTestnet -> "scroll/test"
        Blockchain.ZkLinkNova -> "zklink"
        Blockchain.ZkLinkNovaTestnet -> "zklink/test"
        Blockchain.Pepecoin -> "pepecoin"
        Blockchain.PepecoinTestnet -> "pepecoin/test"
    }
}

/**
 * CoinId is id from tangem backend response coin "id" field
 */
@Suppress("ComplexMethod", "LongMethod")
fun Blockchain.toCoinId(): String {
    return when (this) {
        Blockchain.Binance, Blockchain.BinanceTestnet, Blockchain.BSC, Blockchain.BSCTestnet -> "binancecoin"
        Blockchain.Bitcoin, Blockchain.BitcoinTestnet -> "bitcoin"
        Blockchain.BitcoinCash, Blockchain.BitcoinCashTestnet -> "bitcoin-cash"
        Blockchain.Ethereum, Blockchain.EthereumTestnet -> "ethereum"
        Blockchain.EthereumClassic, Blockchain.EthereumClassicTestnet -> "ethereum-classic"
        Blockchain.Stellar, Blockchain.StellarTestnet -> "stellar"
        Blockchain.Cardano -> "cardano"
        Blockchain.Polygon, Blockchain.PolygonTestnet -> NEW_POLYGON_NAME
        Blockchain.Alephium, Blockchain.AlephiumTestnet -> "alephium"
        Blockchain.Arbitrum, Blockchain.ArbitrumTestnet -> "arbitrum-one"
        Blockchain.Avalanche, Blockchain.AvalancheTestnet -> "avalanche-2"
        Blockchain.Solana, Blockchain.SolanaTestnet -> "solana"
        Blockchain.Fantom, Blockchain.FantomTestnet -> "fantom"
        Blockchain.Tron, Blockchain.TronTestnet -> "tron"
        Blockchain.Polkadot, Blockchain.PolkadotTestnet -> "polkadot"
        Blockchain.Ducatus -> "ducatus"
        Blockchain.Litecoin -> "litecoin"
        Blockchain.RSK -> "rootstock"
        Blockchain.Tezos -> "tezos"
        Blockchain.XRP -> "ripple"
        Blockchain.Dogecoin -> "dogecoin"
        Blockchain.Gnosis -> "xdai"
        Blockchain.EthereumPow, Blockchain.EthereumPowTestnet -> "ethereum-pow-iou"
        Blockchain.Dischain -> "ethereumfair" // for backend compatibility
        Blockchain.Kusama -> "kusama"
        Blockchain.Optimism, Blockchain.OptimismTestnet -> "optimistic-ethereum"
        Blockchain.Dash -> "dash"
        Blockchain.Kaspa, Blockchain.KaspaTestnet -> "kaspa"
        Blockchain.TON, Blockchain.TONTestnet -> "the-open-network"
        Blockchain.Kava, Blockchain.KavaTestnet -> "kava"
        Blockchain.Ravencoin, Blockchain.RavencoinTestnet -> "ravencoin"
        Blockchain.Cosmos, Blockchain.CosmosTestnet -> "cosmos"
        Blockchain.TerraV1 -> "terra-luna"
        Blockchain.TerraV2 -> "terra-luna-2"
        Blockchain.Cronos -> "crypto-com-chain"
        Blockchain.Telos, Blockchain.TelosTestnet -> "telos"
        Blockchain.AlephZero, Blockchain.AlephZeroTestnet -> "aleph-zero"
        Blockchain.OctaSpace, Blockchain.OctaSpaceTestnet -> "octaspace"
        Blockchain.Chia, Blockchain.ChiaTestnet -> "chia"
        Blockchain.Near -> "near"
        Blockchain.NearTestnet -> "near/test"
        Blockchain.Decimal, Blockchain.DecimalTestnet -> "decimal"
        Blockchain.XDC, Blockchain.XDCTestnet -> "xdce-crowd-sale"
        Blockchain.VeChain, Blockchain.VeChainTestnet -> "vechain"
        Blockchain.Aptos -> "aptos"
        Blockchain.AptosTestnet -> "aptos/test"
        Blockchain.Playa3ull -> "playa3ull-games-2"
        Blockchain.Shibarium -> "bone-shibaswap"
        Blockchain.ShibariumTestnet -> "bone-shibaswap/test"
        Blockchain.Algorand -> "algorand"
        Blockchain.AlgorandTestnet -> "algorand/test"
        Blockchain.Unknown -> "unknown"
        Blockchain.Hedera -> "hedera-hashgraph"
        Blockchain.HederaTestnet -> "hedera-hashgraph/test"
        Blockchain.Aurora, Blockchain.AuroraTestnet -> "aurora-ethereum"
        Blockchain.Areon, Blockchain.AreonTestnet -> "areon-network"
        Blockchain.PulseChain, Blockchain.PulseChainTestnet -> "pulsechain"
        Blockchain.ZkSyncEra, Blockchain.ZkSyncEraTestnet -> "zksync-ethereum"
        Blockchain.Moonbeam, Blockchain.MoonbeamTestnet -> "moonbeam"
        Blockchain.Manta, Blockchain.MantaTestnet -> "manta-pacific"
        Blockchain.PolygonZkEVM, Blockchain.PolygonZkEVMTestnet -> "polygon-zkevm-ethereum"
        Blockchain.Nexa, Blockchain.NexaTestnet -> "nexa" // FIXME
        Blockchain.Radiant -> "radiant"
        Blockchain.Fact0rn -> "fact0rn"
        Blockchain.Moonriver, Blockchain.MoonriverTestnet -> "moonriver"
        Blockchain.Mantle, Blockchain.MantleTestnet -> "mantle"
        Blockchain.Flare, Blockchain.FlareTestnet -> "flare-networks"
        Blockchain.Taraxa, Blockchain.TaraxaTestnet -> "taraxa"
        Blockchain.Base, Blockchain.BaseTestnet -> "base-ethereum"
        Blockchain.Koinos, Blockchain.KoinosTestnet -> "koinos"
        Blockchain.Joystream -> "joystream"
        Blockchain.Bittensor -> "bittensor"
        Blockchain.Filecoin -> "filecoin"
        Blockchain.Blast, Blockchain.BlastTestnet -> "blast-ethereum"
        Blockchain.Cyber, Blockchain.CyberTestnet -> "cyber-ethereum"
        Blockchain.Sei, Blockchain.SeiTestnet -> "sei-network"
        Blockchain.InternetComputer -> "internet-computer"
        Blockchain.Sui, Blockchain.SuiTestnet -> "sui"
        Blockchain.EnergyWebChain, Blockchain.EnergyWebChainTestnet -> "energy-web-token"
        Blockchain.EnergyWebX, Blockchain.EnergyWebXTestnet -> "energy-web-token"
        Blockchain.Casper, Blockchain.CasperTestnet -> "casper-network"
        Blockchain.Core, Blockchain.CoreTestnet -> "coredaoorg"
        Blockchain.Chiliz, Blockchain.ChilizTestnet -> "chiliz"
        Blockchain.VanarChain, Blockchain.VanarChainTestnet -> "vanar-chain"
        Blockchain.Xodex -> "xodex"
        Blockchain.Canxium -> "canxium"
        Blockchain.Clore -> "clore-ai"
        Blockchain.OdysseyChain, Blockchain.OdysseyChainTestnet -> "dione"
        Blockchain.Bitrock, Blockchain.BitrockTestnet -> "bitrock"
        Blockchain.Sonic, Blockchain.SonicTestnet -> "sonic-3"
        Blockchain.ApeChain, Blockchain.ApeChainTestnet -> "apecoin"
        Blockchain.Scroll, Blockchain.ScrollTestnet -> "scroll-ethereum"
        Blockchain.ZkLinkNova, Blockchain.ZkLinkNovaTestnet -> "zklink-ethereum"
        Blockchain.Pepecoin, Blockchain.PepecoinTestnet -> "pepecoin-network"
    }
}

/**
 * New CoinId to existing coin "id" field.
 * To support both old and new coin id.
 */
fun Blockchain.toMigratedCoinId(): String = when (this) {
    Blockchain.Polygon, Blockchain.PolygonTestnet -> NEW_POLYGON_NAME
    else -> toCoinId()
}

fun Blockchain.amountToCreateAccount(walletManager: WalletManager, token: Token? = null): BigDecimal? {
    return when (this) {
        Blockchain.Stellar -> {
            val reserve = if (walletManager is ReserveAmountProvider) {
                walletManager.getReserveAmount()
            } else {
                BigDecimal.ONE
            }
            if (token?.symbol == NODL) BigDecimal(NODL_AMOUNT_TO_CREATE_ACCOUNT) else reserve
        }
        Blockchain.XRP -> {
            if (walletManager is ReserveAmountProvider) {
                walletManager.getReserveAmount()
            } else {
                BigDecimal.ONE
            }
        }
        Blockchain.Near, Blockchain.NearTestnet -> 0.00182.toBigDecimal()
        Blockchain.Aptos, Blockchain.AptosTestnet,
        Blockchain.Filecoin,
        Blockchain.Casper, Blockchain.CasperTestnet,
        -> BigDecimal.ZERO
        else -> null
    }
}

const val OLD_POLYGON_NAME = "matic-network"
const val NEW_POLYGON_NAME = "polygon-ecosystem-token"

private const val NODL = "NODL"
private const val NODL_AMOUNT_TO_CREATE_ACCOUNT = 1.5
