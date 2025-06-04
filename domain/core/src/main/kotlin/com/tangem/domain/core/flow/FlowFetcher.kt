package com.tangem.domain.core.flow

import arrow.core.Either

/**
 * Flow fetcher
 *
 * @param Params data that required to fetch flow
 *
 * @author Andrew Khokhlov on 06/03/2025
 */
interface FlowFetcher<Params : Any> {

    suspend operator fun invoke(params: Params): Either<Throwable, Unit>
}
