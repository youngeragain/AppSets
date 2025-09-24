package xcj.app.appsets.ui.compose.search

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.im.IMOnlineState
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.custom_component.ImageButtonComponent
import xcj.app.appsets.ui.model.state.AccountStatus
import xcj.app.appsets.util.ktx.toast

private const val TAG = "NavigationSearchBar"

@Composable
fun NavigationSearchBar(
    enable: Boolean,
    inSearchModel: Boolean,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onSearchBarClick: () -> Unit,
    onBioClick: () -> Unit,
) {
    val searchUseCase = LocalUseCaseOfSearch.current
    val searchState by searchUseCase.searchPageState
    var sizeOfSearchBar by remember {
        mutableStateOf(IntSize.Zero)
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedContent(
                targetState = inSearchModel,
                contentAlignment = Alignment.CenterStart
            ) { targetIsSearchMode ->
                if (targetIsSearchMode) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        SearchInputBar(
                            searchPageState = searchState,
                            sizeOfSearchBar = sizeOfSearchBar,
                            onBackClick = onBackClick,
                            onInputContent = onInputContent,
                            onSearchBarSizeChanged = {
                                sizeOfSearchBar = it
                            }
                        )
                    }
                } else {
                    SearchClickableBar(
                        enable = enable,
                        onSearchBarClick = onSearchBarClick
                    )
                }
            }
            LocalAccountUserAvatar(onClick = onBioClick)
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun SearchClickableBar(
    enable: Boolean,
    onSearchBarClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.outline,
                CircleShape
            )
            .widthIn(min = 100.dp, max = 150.dp)
            .height(42.dp)
            .clip(CircleShape)
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
}

@Composable
fun LocalAccountUserAvatar(onClick: (() -> Unit)? = null) {
    val loginState by LocalAccountManager.accountStatus
    val imOnlineState by BrokerTest.imOnLineState
    val targetBorderColor = remember {
        derivedStateOf {
            if (loginState is AccountStatus.Logged &&
                imOnlineState is IMOnlineState.Online
            ) {
                Color.Green
            } else {
                Color.Red
            }
        }
    }
    val resource by remember {
        derivedStateOf {
            if (loginState is AccountStatus.Logged) {
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
    val modifier = if (loginState is AccountStatus.Logged) {
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