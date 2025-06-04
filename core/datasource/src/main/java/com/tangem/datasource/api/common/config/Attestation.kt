package com.tangem.datasource.api.common.config

/**
 * Attestation [ApiConfig] that used in CardSDK
 *
 * @author Andrew Khokhlov on 02/04/2025
 */
internal class Attestation : ApiConfig() {

    override val defaultEnvironment: ApiEnvironment = ApiEnvironment.PROD

    override val environmentConfigs: List<ApiEnvironmentConfig> = listOf(
        createDevEnvironment(),
        createProdEnvironment(),
    )

    private fun createDevEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.DEV,
        baseUrl = "https://api.tests-d.com/",
    )

    private fun createProdEnvironment(): ApiEnvironmentConfig = ApiEnvironmentConfig(
        environment = ApiEnvironment.PROD,
        baseUrl = "https://api.tangem-tech.com/",
    )
}
