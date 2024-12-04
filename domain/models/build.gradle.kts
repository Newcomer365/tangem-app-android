import com.tangem.plugin.configuration.configurations.extension.kaptForObfuscatingVariants

plugins {
    alias(deps.plugins.kotlin.jvm)
    alias(deps.plugins.kotlin.serialization)
    alias(deps.plugins.kotlin.kapt)
    id("configuration")
}

dependencies {
    implementation(deps.tangem.card.core)
    implementation(deps.moshi.kotlin)
    implementation(deps.kotlin.serialization)
    kapt(deps.moshi.kotlin.codegen)
}
