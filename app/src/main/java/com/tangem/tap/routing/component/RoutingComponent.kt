package com.tangem.tap.routing.component

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.fragment.app.Fragment
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.tangem.common.routing.AppRoute
import com.tangem.core.decompose.context.AppComponentContext
import com.tangem.core.decompose.navigation.Router
import com.tangem.core.ui.decompose.ComposableContentComponent
import com.tangem.utils.Provider

internal interface RoutingComponent : ComposableContentComponent {

    // TODO: Remove after full navigation refactoring
    val router: Router

    // TODO: Remove after full navigation refactoring
    val stack: Value<ChildStack<AppRoute, Child>>

    @Immutable
    sealed class Child {

        // TODO: Remove and find initial intent in RoutingComponent: https://tangem.atlassian.net/browse/AND-9520
        data object Initial : Child()

        data class LegacyFragment(
            val name: String,
            val fragmentProvider: Provider<Fragment>,
        ) : Child()

        data class LegacyIntent(val intent: Intent) : Child()

        data class ComposableComponent(
            val component: ComposableContentComponent,
        ) : Child()
    }

    interface Factory {
        fun create(context: AppComponentContext, initialStack: List<AppRoute>?): RoutingComponent
    }
}
