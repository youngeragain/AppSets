package xcj.app.appsets.ui.compose.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ui.compose.LocalPageRouteNameNeedLoggedProvider
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch

@Composable
fun LoginInterceptorPage(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!LocalAccountManager.isLogged() && LocalPageRouteNameNeedLoggedProvider.current.contains(
            navBackStackEntry.destination.route
        )
    ) {
        HideNavBarWhenOnLaunch()
        Box(modifier = Modifier.fillMaxSize()) {
            val annotatedString = AnnotatedString(
                stringResource(xcj.app.appsets.R.string.requires_login_to_appsets)
            )
            Text(
                text = annotatedString,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable(
                        onClick = {
                            navController.navigate(PageRouteNames.LoginPage)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary
            )
            DesignBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }
    } else {
        content()
    }
}

