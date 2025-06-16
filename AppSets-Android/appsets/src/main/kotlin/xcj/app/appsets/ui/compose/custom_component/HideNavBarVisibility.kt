package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.usecase.NavigationUseCase

@Composable
fun HideNavBarWhenOnLaunch(
    navigationUseCase: NavigationUseCase = LocalUseCaseOfNavigation.current,
) {
    LaunchedEffect(key1 = true) {
        navigationUseCase.barVisible.value = false
    }
}

@Composable
fun ShowNavBarWhenOnLaunch(
    navigationUseCase: NavigationUseCase = LocalUseCaseOfNavigation.current,
) {
    LaunchedEffect(key1 = true) {
        navigationUseCase.barVisible.value = true
    }
}

@Composable
fun ShowNavBarWhenOnDispose(
    navigationUseCase: NavigationUseCase = LocalUseCaseOfNavigation.current,
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            navigationUseCase.barVisible.value = true
        }
    })
}
