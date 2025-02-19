package xcj.app.appsets.ui.compose.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.ui.compose.custom_component.ImageButtonComponent
import xcj.app.appsets.ui.model.LoginStatusState

private const val TAG = "StandardSearchBar"

@Composable
fun StandardSearchBar(
    enable: Boolean,
    onSearchBarClick: () -> Unit,
    onBioClick: () -> Unit
) {
    val context = LocalContext.current
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .widthIn(min = 100.dp, max = 150.dp)
                    .height(42.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        if (!enable) {
                            context
                                .getString(xcj.app.appsets.R.string.need_to_update_app)
                                .toast()
                        } else {
                            onSearchBarClick()
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_search_24),
                    contentDescription = stringResource(id = xcj.app.appsets.R.string.search)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = xcj.app.appsets.R.string.search), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(10.dp))
            }
            LocalAccountUserAvatar(onClick = onBioClick)
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun LocalAccountUserAvatar(onClick: (() -> Unit)? = null) {
    val loginState by LocalAccountManager.loginStatusState
    val targetBorderColor = remember {
        derivedStateOf {
            if (loginState is LoginStatusState.Logged && BrokerTest.onlineState.value) {
                Color.Green
            } else {
                Color.Red
            }
        }
    }
    val resource by remember {
        derivedStateOf {
            if (loginState is LoginStatusState.Logged) {
                loginState.userInfo.bioUrl
            } else {
                xcj.app.compose_share.R.drawable.ic_outline_face_24
            }
        }
    }

    val borderColor by animateColorAsState(
        targetValue = targetBorderColor.value,
        label = "border_color_animate"
    )
    val modifier = if (loginState is LoginStatusState.Logged) {
        Modifier.border(2.dp, borderColor, CircleShape)
    } else {
        Modifier
    }
    ImageButtonComponent(
        modifier = modifier,
        useImage = false,
        resource = resource,
        onClick = {
            onClick?.invoke()
        }
    )
}