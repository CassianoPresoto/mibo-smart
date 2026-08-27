package intelbras.mobi.smart.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.ui.feature.devices.DeviceKind
import intelbras.mobi.smart.ui.feature.devices.DeviceUiModel
import intelbras.mobi.smart.ui.component.MiboNavigationBar
import intelbras.mobi.smart.ui.component.MiboNavigationTab
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.navigation_account
import mibosmart.shared.generated.resources.navigation_activity
import mibosmart.shared.generated.resources.navigation_devices
import org.jetbrains.compose.resources.stringResource
import intelbras.mobi.smart.ui.feature.account.AccountRoute as AccountDestination
import intelbras.mobi.smart.ui.feature.activity.ActivityRoute as ActivityDestination
import intelbras.mobi.smart.ui.feature.devices.DeviceListRoute as DeviceListDestination
import intelbras.mobi.smart.ui.feature.session.SessionCheckRoute as SessionCheckDestination
import intelbras.mobi.smart.ui.feature.lock.LockRoute as LockDestination
import intelbras.mobi.smart.ui.feature.lock.history.OpeningHistoryRoute as LockHistoryDestination
import intelbras.mobi.smart.ui.feature.token.TokenEntryRoute as TokenEntryDestination
import intelbras.mobi.smart.ui.feature.video.LiveVideoRoute as LiveVideoDestination

private const val SLIDE_BACK_FRACTION = 3

@Composable
internal fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val currentEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MiboTheme.colors.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        bottomBar = {
            if (currentEntry.isTopLevel()) {
                AppNavigationBar(
                    currentEntry = currentEntry,
                    onTabSelected = navController::openTab,
                )
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = SessionCheckRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MiboTheme.colors.background),
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
                    onAccountClick = { navController.openTab(AccountRoute) },
                    onRenewSession = { navController.openTokenEntry(sessionExpired = true) },
                )
            }

            composable<ActivityRoute> { ActivityDestination() }

            composable<AccountRoute> {
                AccountDestination(
                    onSignedOut = { navController.openTokenEntry(sessionExpired = false) },
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
                    onSeeAllHistory = {
                        navController.navigate(
                            LockHistoryRoute(route.address, route.productId, route.name)
                        )
                    },
                    onLeave = { navController.popBackStack() },
                )
            }

            composable<LockHistoryRoute> { entry ->
                val route = entry.toRoute<LockHistoryRoute>()
                LockHistoryDestination(
                    lock = route.reference(),
                    lockName = route.name,
                    onLeave = { navController.popBackStack() },
                )
            }
            }
    }
}

@Composable
private fun AppNavigationBar(currentEntry: NavBackStackEntry?, onTabSelected: (Any) -> Unit) {
    MiboNavigationBar {
        MiboNavigationTab(
            label = stringResource(Res.string.navigation_devices),
            icon = Icons.Filled.Devices,
            selected = currentEntry.shows(DeviceListRoute::class),
            onClick = { onTabSelected(DeviceListRoute) },
        )
        MiboNavigationTab(
            label = stringResource(Res.string.navigation_activity),
            icon = Icons.Filled.History,
            selected = currentEntry.shows(ActivityRoute::class),
            onClick = { onTabSelected(ActivityRoute) },
        )
        MiboNavigationTab(
            label = stringResource(Res.string.navigation_account),
            icon = Icons.Filled.Person,
            selected = currentEntry.shows(AccountRoute::class),
            onClick = { onTabSelected(AccountRoute) },
        )
    }
}

private fun NavBackStackEntry?.isTopLevel(): Boolean =
    shows(DeviceListRoute::class) || shows(ActivityRoute::class) || shows(AccountRoute::class)

private fun NavBackStackEntry?.shows(route: kotlin.reflect.KClass<*>): Boolean =
    this?.destination?.hasRoute(route) == true

private fun NavHostController.openTab(route: Any) {
    navigate(route) {
        popUpTo(DeviceListRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.openTokenEntry(sessionExpired: Boolean) {
    navigate(TokenEntryRoute(sessionExpired)) {
        popUpTo(DeviceListRoute) { inclusive = true }
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

private fun LockHistoryRoute.reference() =
    DeviceReference(serialNumber = address, productId = productId)
