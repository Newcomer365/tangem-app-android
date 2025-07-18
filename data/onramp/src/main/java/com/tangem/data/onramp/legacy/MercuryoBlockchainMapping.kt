package com.tangem.data.onramp.legacy

import com.tangem.blockchain.common.Blockchain

/**
 * Legacy onboarding note/twins to get top up URL from mercuryo
 * Map [Blockchain] to ids from [link](https://api.mercuryo.io/v1.6/lib/currencies) [crypto_currencies]
 */
@Suppress("CyclomaticComplexMethod")
public val Blockchain.mercuryoNetwork: String?
    get() {
        return when (this) {
            Blockchain.Algorand -> "ALGORAND"
            Blockchain.Arbitrum -> "ARBITRUM"
            Blockchain.Avalanche -> "AVALANCHE"
            Blockchain.BSC -> "BINANCESMARTCHAIN"
            Blockchain.Bitcoin -> "BITCOIN"
            Blockchain.BitcoinCash -> "BITCOINCASH"
            Blockchain.Cardano -> "CARDANO"
            Blockchain.Cosmos -> "COSMOS"
            Blockchain.Dash -> "DASH"
            Blockchain.Dogecoin -> "DOGECOIN"
            Blockchain.Ethereum -> "ETHEREUM"
            Blockchain.Fantom -> "FANTOM"
            Blockchain.Kusama -> "KUSAMA"
            Blockchain.Litecoin -> "LITECOIN"
            Blockchain.Near -> "NEAR_PROTOCOL"
            Blockchain.TON -> "NEWTON"
            Blockchain.Optimism -> "OPTIMISM"
            Blockchain.Polkadot -> "POLKADOT"
            Blockchain.Polygon -> "POLYGON"
            Blockchain.XRP -> "RIPPLE"
            Blockchain.Solana -> "SOLANA"
            Blockchain.Stellar -> "STELLAR"
            Blockchain.Tezos -> "TEZOS"
            Blockchain.Tron -> "TRON"
            Blockchain.ZkSyncEra -> "ZKSYNC"
            Blockchain.Base -> "BASE"
            Blockchain.ArbitrumTestnet -> null
            Blockchain.AvalancheTestnet -> null
            Blockchain.Binance -> null
            Blockchain.BinanceTestnet -> null
            Blockchain.BSCTestnet -> null
            Blockchain.BitcoinTestnet -> null
            Blockchain.BitcoinCashTestnet -> null
            Blockchain.CosmosTestnet -> null
            Blockchain.Ducatus -> null
            Blockchain.EthereumTestnet -> null
            Blockchain.EthereumClassic -> null
            Blockchain.EthereumClassicTestnet -> null
            Blockchain.FantomTestnet -> null
            Blockchain.NearTestnet -> null
            Blockchain.PolkadotTestnet -> null
            Blockchain.Kava -> null
            Blockchain.KavaTestnet -> null
            Blockchain.PolygonTestnet -> null
            Blockchain.RSK -> null
            Blockchain.Sei -> null
            Blockchain.SeiTestnet -> null
            Blockchain.StellarTestnet -> null
            Blockchain.SolanaTestnet -> null
            Blockchain.TronTestnet -> null
            Blockchain.Gnosis -> null
            Blockchain.OptimismTestnet -> null
            Blockchain.Dischain -> null
            Blockchain.EthereumPow -> null
            Blockchain.EthereumPowTestnet -> null
            Blockchain.Kaspa -> null
            Blockchain.Telos -> null
            Blockchain.TelosTestnet -> null
            Blockchain.TONTestnet -> null
            Blockchain.Ravencoin -> null
            Blockchain.RavencoinTestnet -> null
            Blockchain.TerraV1 -> null
            Blockchain.TerraV2 -> null
            Blockchain.Cronos -> null
            Blockchain.AlephZero -> null
            Blockchain.AlephZeroTestnet -> null
            Blockchain.OctaSpace -> null
            Blockchain.OctaSpaceTestnet -> null
            Blockchain.Chia -> null
            Blockchain.ChiaTestnet -> null
            Blockchain.Decimal -> null
            Blockchain.DecimalTestnet -> null
            Blockchain.XDC -> null
            Blockchain.XDCTestnet -> null
            Blockchain.VeChain -> null
            Blockchain.VeChainTestnet -> null
            Blockchain.Aptos -> null
            Blockchain.AptosTestnet -> null
            Blockchain.Playa3ull -> null
            Blockchain.Shibarium -> null
            Blockchain.ShibariumTestnet -> null
            Blockchain.AlgorandTestnet -> null
            Blockchain.Hedera -> null
            Blockchain.HederaTestnet -> null
            Blockchain.Aurora -> null
            Blockchain.AuroraTestnet -> null
            Blockchain.Alephium -> null
            Blockchain.AlephiumTestnet -> null
            Blockchain.Areon -> null
            Blockchain.AreonTestnet -> null
            Blockchain.PulseChain -> null
            Blockchain.PulseChainTestnet -> null
            Blockchain.ZkSyncEraTestnet -> null
            Blockchain.Nexa -> null
            Blockchain.NexaTestnet -> null
            Blockchain.Moonbeam -> null
            Blockchain.MoonbeamTestnet -> null
            Blockchain.Manta -> null
            Blockchain.MantaTestnet -> null
            Blockchain.PolygonZkEVM -> null
            Blockchain.PolygonZkEVMTestnet -> null
            Blockchain.Radiant -> null
            Blockchain.Fact0rn -> null
            Blockchain.BaseTestnet -> null
            Blockchain.Moonriver -> null
            Blockchain.MoonriverTestnet -> null
            Blockchain.Mantle -> null
            Blockchain.MantleTestnet -> null
            Blockchain.Flare -> null
            Blockchain.FlareTestnet -> null
            Blockchain.Taraxa -> null
            Blockchain.TaraxaTestnet -> null
            Blockchain.Koinos -> null
            Blockchain.KoinosTestnet -> null
            Blockchain.Joystream -> null
            Blockchain.Bittensor -> null
            Blockchain.Filecoin -> null
            Blockchain.Blast -> null
            Blockchain.BlastTestnet -> null
            Blockchain.Cyber -> null
            Blockchain.CyberTestnet -> null
            Blockchain.InternetComputer -> null
            Blockchain.Sui -> null
            Blockchain.SuiTestnet -> null
            Blockchain.EnergyWebChain -> null
            Blockchain.EnergyWebChainTestnet -> null
            Blockchain.EnergyWebX -> null
            Blockchain.EnergyWebXTestnet -> null
            Blockchain.Casper -> null
            Blockchain.CasperTestnet -> null
            Blockchain.Core -> null
            Blockchain.CoreTestnet -> null
            Blockchain.Chiliz -> null
            Blockchain.ChilizTestnet -> null
            Blockchain.VanarChain -> null
            Blockchain.VanarChainTestnet -> null
            Blockchain.Unknown -> null
            Blockchain.Xodex -> null
            Blockchain.Canxium -> null
            Blockchain.Clore -> null
            Blockchain.OdysseyChain, Blockchain.OdysseyChainTestnet -> null
            Blockchain.Bitrock, Blockchain.BitrockTestnet -> null
            Blockchain.Sonic, Blockchain.SonicTestnet -> null
            Blockchain.ApeChain, Blockchain.ApeChainTestnet -> null
            Blockchain.Scroll, Blockchain.ScrollTestnet -> null
            Blockchain.ZkLinkNova, Blockchain.ZkLinkNovaTestnet -> null
            Blockchain.KaspaTestnet -> null
            Blockchain.Pepecoin, Blockchain.PepecoinTestnet -> null
        }
    }
