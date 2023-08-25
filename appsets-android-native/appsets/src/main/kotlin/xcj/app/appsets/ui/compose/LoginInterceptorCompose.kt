package xcj.app.appsets.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavBackStackEntry
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager

@UnstableApi
@Composable
fun LoginInterceptorCompose(
    tabVisibilityState: MutableState<Boolean>,
    navBackStackEntry: NavBackStackEntry,
    onBackAction: () -> Unit,
    onLoginClick: () -> Unit,
    content: @Composable () -> Unit
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    val current = LocalPageRouteNameNeedLoggedProvider.current
    if (!LocalAccountManager.isLogged() && current.contains(navBackStackEntry.destination.route)) {
        SideEffect {
            tabVisibilityState.value = false
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val annotatedString = AnnotatedString(
                    "需要登录AppSets",
                    listOf(
                        AnnotatedString.Range(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ), 2, 11
                        )
                    )
                )
                Text(annotatedString, Modifier.clickable(onClick = onLoginClick))
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_arrow_24),
                    contentDescription = "go back",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = onBackAction)
                        .padding(16.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    } else {
        content()
    }
}