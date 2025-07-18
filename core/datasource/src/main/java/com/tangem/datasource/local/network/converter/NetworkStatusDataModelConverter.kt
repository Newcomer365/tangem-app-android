package com.tangem.datasource.local.network.converter

import com.tangem.datasource.local.network.entity.NetworkStatusDM
import com.tangem.domain.tokens.model.NetworkStatus
import com.tangem.utils.converter.Converter

/**
 * Converter from [NetworkStatus] to [NetworkStatusDM]
 *
 * @author Andrew Khokhlov on 18/02/2025
 */
object NetworkStatusDataModelConverter : Converter<NetworkStatus, NetworkStatusDM?> {

    override fun convert(value: NetworkStatus): NetworkStatusDM? {
        return when (val status = value.value) {
            is NetworkStatus.Verified -> {
                NetworkStatusDM.Verified(
                    networkId = value.network.id,
                    derivationPath = NetworkDerivationPathConverter.convertBack(value = value.network.derivationPath),
                    selectedAddress = status.address.defaultAddress.value,
                    availableAddresses = NetworkAddressConverter(selectedAddress = status.address.defaultAddress.value)
                        .convertBack(value = status.address),
                    amounts = NetworkAmountsConverter.convertBack(value = status.amounts),
                )
            }
            is NetworkStatus.NoAccount -> {
                NetworkStatusDM.NoAccount(
                    networkId = value.network.id,
                    derivationPath = NetworkDerivationPathConverter.convertBack(value = value.network.derivationPath),
                    selectedAddress = status.address.defaultAddress.value,
                    availableAddresses = NetworkAddressConverter(selectedAddress = status.address.defaultAddress.value)
                        .convertBack(value = status.address),
                    amountToCreateAccount = status.amountToCreateAccount,
                    errorMessage = status.errorMessage,
                )
            }
            else -> null
        }
    }
}
