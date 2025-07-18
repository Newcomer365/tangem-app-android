package com.tangem.datasource.api.common.config

import com.tangem.datasource.api.common.AuthProvider
import com.tangem.datasource.utils.RequestHeader
import com.tangem.utils.version.AppVersionProvider

/** TangemTech [ApiConfig] */
internal class TangemTech(
    private val appVersionProvider: AppVersionProvider,
    private val authProvider: AuthProvider,
) : ApiConfig() {

    override val defaultEnvironment: ApiEnvironment = ApiEnvironment.PROD

    override val environmentConfigs = listOf(
        createDevEnvironment(),
        createProdEnvironment(),
    )

    private fun createProdEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.PROD,
        baseUrl = "https://api.tangem.org/v1/",
        headers = createHeaders(),
    )

    private fun createDevEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.DEV,
        baseUrl = "https://api.tests-d.com/v1/",
        headers = createHeaders(),
    )

    private fun createHeaders() = buildMap {
        putAll(from = RequestHeader.AppVersionPlatformHeaders(appVersionProvider).values)
        putAll(from = RequestHeader.AuthenticationHeader(authProvider).values)
    }
}
