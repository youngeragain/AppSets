package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.usecase.NavigationUseCase

@Composable
fun HideNavBar(
    key: Any? = Unit,
    navigationUseCase: NavigationUseCase = LocalUseCaseOfNavigation.current,
) {
    LaunchedEffect(key1 = key) {
        navigationUseCase.barVisible.value = false
    }
}

@Composable
fun ShowNavBar(
    key: Any? = Unit,
    navigationUseCase: NavigationUseCase = LocalUseCaseOfNavigation.current,
) {
    LaunchedEffect(key1 = key) {
        navigationUseCase.barVisible.value = true
    }
}