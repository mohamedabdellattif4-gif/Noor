package com.noor.app

import com.noor.core.navigation.NoorRoutes

/**
 * Immutable navigation input owned by [MainActivity].
 *
 * The start destination is fixed for the lifetime of an activity instance, while
 * [adhkarRequestId] is a one-shot signal that changes for every warm notification intent.
 * This prevents repeated notification taps from being lost when the requested route has
 * already been opened once.
 */
internal data class AppLaunchNavigation(
    val startDestination: String,
    val adhkarRequestId: Long = NO_REQUEST,
) {
    fun requestAdhkar(): AppLaunchNavigation = copy(
        adhkarRequestId = if (adhkarRequestId == Long.MAX_VALUE) {
            FIRST_REQUEST
        } else {
            adhkarRequestId + 1L
        },
    )

    companion object {
        const val NO_REQUEST: Long = 0L
        private const val FIRST_REQUEST: Long = 1L

        fun initial(openAdhkar: Boolean): AppLaunchNavigation = AppLaunchNavigation(
            startDestination = if (openAdhkar) NoorRoutes.ADHKAR else NoorRoutes.HOME,
        )
    }
}
