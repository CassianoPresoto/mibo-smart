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
import intelbras.mobi.smart.ui.feature.devices.DeviceListItem
import intelbras.mobi.smart.ui.feature.devices.DeviceListRoute as DeviceListDestination
import intelbras.mobi.smart.ui.feature.session.SessionCheckRoute as SessionCheckDestination
import intelbras.mobi.smart.ui.feature.session.SessionViewModel
import intelbras.mobi.smart.ui.feature.token.TokenEntryRoute as TokenEntryDestination
import intelbras.mobi.smart.ui.feature.video.LiveVideoRoute as LiveVideoDestination
import org.koin.compose.viewmodel.koinViewModel

private const val SLIDE_BACK_FRACTION = 3

@Composable
internal fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
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
                onDeviceSelected = { device -> navController.navigate(device.toRoute()) },
                onSignOut = {
                    sessionViewModel.onSignOut()
                    navController.navigate(TokenEntryRoute()) {
                        popUpTo(DeviceListRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<LiveVideoRoute> { entry ->
            val route = entry.toRoute<LiveVideoRoute>()
            LiveVideoDestination(
                device = DeviceReference(
                    serialNumber = route.serialNumber,
                    productId = route.productId,
                ),
                deviceName = route.name,
                deviceModel = route.model,
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

private fun DeviceListItem.toRoute() = LiveVideoRoute(
    serialNumber = serialNumber,
    productId = productId,
    name = name,
    model = model,
)
