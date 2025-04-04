package com.tangem.tap.data

import com.tangem.blockchain.common.logging.BlockchainSDKLogger
import com.tangem.datasource.local.logs.AppLogsStore

/**
 * BlockchainSDK logger implementation
 *
 * @property appLogsStore app logs store
 *
 * @author Andrew Khokhlov on 28/02/2024
 */
internal class TangemBlockchainSDKLogger(
    private val appLogsStore: AppLogsStore,
) : BlockchainSDKLogger {

    override fun log(level: BlockchainSDKLogger.Level, message: String) {
        appLogsStore.saveLogMessage(tag = "BlockchainSDK_${level.name}", message)
    }
}
