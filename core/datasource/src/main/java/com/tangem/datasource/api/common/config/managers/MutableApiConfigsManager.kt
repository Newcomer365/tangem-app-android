package com.tangem.datasource.api.common.config.managers

import com.tangem.datasource.api.common.config.ApiConfig
import com.tangem.datasource.api.common.config.ApiEnvironment
import kotlinx.coroutines.flow.Flow

/**
 * Mutable [ApiConfigsManager] for change information about the current api environment
 *
 * @author Andrew Khokhlov on 08/08/2024
 */
interface MutableApiConfigsManager : ApiConfigsManager {

    /** Configs */
    val configs: Flow<List<ApiConfig>>

    /** Change api environment [environment] by [id] */
    suspend fun changeEnvironment(id: String, environment: ApiEnvironment)
}