package com.tangem.core.ui.components.fields

import com.tangem.utils.coroutines.JobHolder
import com.tangem.utils.coroutines.withDebounce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Search tokens manager
 *
 * @author Andrew Khokhlov on 23/10/2024
 */
class InputManager @Inject constructor() {

    val query: Flow<String>
        get() = _query

    private val _query = MutableStateFlow(value = "")

    private val jobHolder = JobHolder()

    suspend fun update(value: String) {
        coroutineScope {
            if (value.isEmpty()) {
                jobHolder.cancel()
                _query.value = value
            } else {
                withDebounce(jobHolder) { _query.value = value }
            }
        }
    }
}
