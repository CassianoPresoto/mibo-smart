package intelbras.mobi.smart.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.ui.feature.devices.DeviceKind
import intelbras.mobi.smart.ui.feature.devices.DeviceUiModel
import intelbras.mobi.smart.ui.feature.account.AccountRoute as AccountDestination
import intelbras.mobi.smart.ui.feature.devices.DeviceListRoute as DeviceListDestination
import intelbras.mobi.smart.ui.feature.session.SessionCheckRoute as SessionCheckDestination
import intelbras.mobi.smart.ui.feature.lock.LockRoute as LockDestination
import intelbras.mobi.smart.ui.feature.token.TokenEntryRoute as TokenEntryDestination
import intelbras.mobi.smart.ui.feature.video.LiveVideoRoute as LiveVideoDestination

private const val SLIDE_BACK_FRACTION = 3

@Composable
internal fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = SessionCheckRoute,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = { slideInHorizontally { width -> width } + fadeIn() },
        exitTransition = { slideOutHorizontally { width -> -width / SLIDE_BACK_FRACTION } + fadeOut() },
        popEnterTransition = { slideInHorizontally { width -> -width / SLIDE_BACK_FRACTION } + fadeIn() },
        popExitTransition = { slideOutHorizontally { width -> width } + fadeOut() },
    ) {
        composable<SessionCheckRoute> {
            SessionCheckDestination(
                onSessionOpen = { navController.openDeviceList(from = SessionCheckRoute) },
                onSessionMissing = { sessionExpired ->
                    navController.navigate(TokenEntryRoute(sessionExpired)) {
                        popUpTo(SessionCheckRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<TokenEntryRoute> { entry ->
            val route = entry.toRoute<TokenEntryRoute>()
            TokenEntryDestination(
                sessionExpired = route.sessionExpired,
                onAuthenticated = { navController.openDeviceList(from = route) },
            )
        }

        composable<DeviceListRoute> {
            DeviceListDestination(
                onDeviceClick = { device ->
                    device.destination()?.let { destination -> navController.navigate(destination) }
                },
                onAccountClick = { navController.navigate(AccountRoute) },
            )
        }

        composable<AccountRoute> {
            AccountDestination(
                onSignedOut = {
                    navController.navigate(TokenEntryRoute()) {
                        popUpTo(DeviceListRoute) { inclusive = true }
                    }
                },
                onLeave = { navController.popBackStack() },
            )
        }

        composable<LiveVideoRoute> { entry ->
            val route = entry.toRoute<LiveVideoRoute>()
            LiveVideoDestination(
                device = route.reference(),
                deviceName = route.name,
                deviceModel = route.model,
                onLeave = { navController.popBackStack() },
            )
        }

        composable<LockRoute> { entry ->
            val route = entry.toRoute<LockRoute>()
            LockDestination(
                lock = route.reference(),
                lockName = route.name,
                lockModel = route.model,
                onLeave = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.openDeviceList(from: Any) {
    navigate(DeviceListRoute) {
        popUpTo(from) { inclusive = true }
    }
}

private fun DeviceUiModel.destination(): Any? = when (kind) {
    DeviceKind.Camera -> LiveVideoRoute(id, productId, name, model)
    DeviceKind.Lock -> LockRoute(id, productId, name, model)
    DeviceKind.Light, DeviceKind.Sensor, DeviceKind.Other -> null
}

private fun LiveVideoRoute.reference() =
    DeviceReference(serialNumber = address, productId = productId)

private fun LockRoute.reference() = DeviceReference(serialNumber = address, productId = productId)
