package com.tangem.domain.nft

import arrow.core.raise.catch
import com.tangem.domain.nft.models.NFTAsset
import com.tangem.domain.nft.repository.NFTRepository
import com.tangem.domain.tokens.model.Network

class GetNFTExploreUrlUseCase(
    private val nftRepository: NFTRepository,
) {

    suspend operator fun invoke(network: Network, assetIdentifier: NFTAsset.Identifier): String? = catch(
        block = {
            nftRepository.getNFTExploreUrl(
                network = network,
                assetIdentifier = assetIdentifier,
            )
        },
        catch = { null },
    )
}
