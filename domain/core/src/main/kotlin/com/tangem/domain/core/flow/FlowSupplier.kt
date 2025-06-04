package com.tangem.domain.core.flow

import kotlinx.coroutines.flow.Flow

/**
 * [Flow] supplier
 *
 * @param Params type of data that required to get [Flow]
 * @param Data   data type of [Flow]
 *
 * @author Andrew Khokhlov on 06/03/2025
 */
interface FlowSupplier<Params : Any, Data : Any> {

    /** Supply [Flow] by [params] */
    operator fun invoke(params: Params): Flow<Data>
}
