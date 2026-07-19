package com.noor.app

import com.noor.core.navigation.NoorRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppLaunchNavigationTest {
    @Test
    fun coldNotificationLaunchUsesAdhkarAsStartDestinationWithoutWarmRequest() {
        val navigation = AppLaunchNavigation.initial(openAdhkar = true)

        assertEquals(NoorRoutes.ADHKAR, navigation.startDestination)
        assertEquals(AppLaunchNavigation.NO_REQUEST, navigation.adhkarRequestId)
    }

    @Test
    fun regularLaunchUsesHomeAsStartDestination() {
        val navigation = AppLaunchNavigation.initial(openAdhkar = false)

        assertEquals(NoorRoutes.HOME, navigation.startDestination)
        assertEquals(AppLaunchNavigation.NO_REQUEST, navigation.adhkarRequestId)
    }

    @Test
    fun everyWarmNotificationProducesANewOneShotRequest() {
        val initial = AppLaunchNavigation.initial(openAdhkar = false)
        val firstRequest = initial.requestAdhkar()
        val secondRequest = firstRequest.requestAdhkar()

        assertNotEquals(initial.adhkarRequestId, firstRequest.adhkarRequestId)
        assertNotEquals(firstRequest.adhkarRequestId, secondRequest.adhkarRequestId)
        assertEquals(NoorRoutes.HOME, secondRequest.startDestination)
    }

    @Test
    fun requestCounterWrapsWithoutReturningToNoRequestSentinel() {
        val atMaximum = AppLaunchNavigation(
            startDestination = NoorRoutes.HOME,
            adhkarRequestId = Long.MAX_VALUE,
        )

        assertEquals(1L, atMaximum.requestAdhkar().adhkarRequestId)
    }
}
