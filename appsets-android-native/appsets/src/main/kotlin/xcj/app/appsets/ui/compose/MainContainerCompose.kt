package xcj.app.appsets.ui.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.delay
import xcj.app.appsets.account.LocalAccountManager

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@UnstableApi
@Composable
fun MainContainerCompose() {
    val context = LocalContext.current
    val viewModel = viewModel<MainViewModel>(context as AppCompatActivity)
    CompositionLocalProvider(
        LocalBackPressedDispatcher provides context.onBackPressedDispatcher
    ) {
        LaunchedEffect(key1 = true, block = {
            viewModel.bottomMenuUseCase.initTabItems()
        })
        val navHostController = rememberNavController()
        SideEffect {
            viewModel.bottomMenuUseCase.invalidateWhenMainCompose(navHostController.currentDestination?.route)
        }

        DisposableEffect(key1 = navHostController, effect = {
            val destinationChangedListener: NavController.OnDestinationChangedListener =
                NavController.OnDestinationChangedListener { _, destination, _ ->
                    viewModel.bottomMenuUseCase.tabItemsState.forEach {
                        it.isSelect.value = it.type == destination.route
                    }
                    Log.e(
                        "DestinationChangedListener",
                        "destination.parent?.route:${viewModel.bottomMenuUseCase.mSavedLastNavDestination}," +
                                " destination.route:${destination.route}"
                    )
                }
            navHostController.addOnDestinationChangedListener(destinationChangedListener)
            onDispose {
                navHostController.removeOnDestinationChangedListener(destinationChangedListener)
            }
        })

        val onTabClick: (TabItemState) -> Unit = { tab ->
            Log.e("OnTabClick", "tab:${tab}")
            if (!tab.isSelect.value) {
                navHostController.navigate(tab.type, navOptions {
                    popUpTo(navHostController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                })
            }
        }
        val newVersionState = viewModel.appSetsUseCase.newVersionState
        LaunchedEffect(key1 = newVersionState.value, block = {
            if (newVersionState.value?.forceUpdate == true) {
                delay(280)
                viewModel.bottomMenuUseCase.tabVisibilityState.value = false
            }
        })
        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_SQUARE -> Unit
            Configuration.ORIENTATION_UNDEFINED -> Unit
            Configuration.ORIENTATION_LANDSCAPE -> {
                MyLandScapeScaffold(
                    appbarDirection = Direction.END,
                    appBar = {
                        NavigationBar(onTabClick = onTabClick)
                    }) {
                    NavigationCompose(navController = navHostController)
                }
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                Scaffold(bottomBar = {
                    NavigationBar(onTabClick = onTabClick)
                }) { _ ->
                    NavigationCompose(navHostController)
                }
            }
        }
        var tokenErrorDialogIsShowing by remember {
            mutableStateOf(false)
        }
        val tokenErrorState by LocalAccountManager.tokenErrorState
        if (tokenErrorState != null && tokenErrorState!!.peekValue() && !tokenErrorDialogIsShowing) {
            SideEffect {
                if (tokenErrorState!!.peekValue()) {
                    val tokenErrorBottomSheetDialog = TokenErrorBottomSheetDialog()
                    tokenErrorBottomSheetDialog.onDestroyListener = {
                        tokenErrorDialogIsShowing = false
                        LocalAccountManager.tokenErrorState.value = null
                    }
                    val supportFragmentManager = context.supportFragmentManager
                    if (!tokenErrorBottomSheetDialog.isVisible) {
                        tokenErrorState!!.useValue()
                        tokenErrorDialogIsShowing = true
                        tokenErrorBottomSheetDialog.show(
                            supportFragmentManager,
                            tokenErrorBottomSheetDialog.tag
                        )
                    }
                }
            }
        }
    }
}