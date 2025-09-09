package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.compose_share.components.DesignHDivider

private const val TAG = "UserInfoHeader"

data class UserAction(val type: String, val name: String) {
    companion object {
        const val ACTION_APPLICATION = "Application"
        const val ACTION_SCREEN = "Screen"
        const val ACTION_FOLLOW_STATE = "FollowState"
        const val ACTION_UPDATE_INFO = "UpdateInfo"
        const val ACTION_FLIP_FOLLOW = "FlipFollow"
        const val ACTION_CHAT = "Chat"
        const val ACTION_ADD_FRIEND = "AddFriend"
        const val ACTION_GOODS = "GoodS"
    }
}

@Composable
private fun makeUserActions(
    userInfo: UserInfo,
    isLoginUserFollowedThisUser: Boolean,
    userFollowers: List<UserInfo>,
    userFollowed: List<UserInfo>,
): List<UserAction> {
    val tabs = remember {
        mutableStateListOf<UserAction>()
    }
    tabs.clear()
    tabs.add(
        UserAction(
            UserAction.ACTION_APPLICATION,
            stringResource(xcj.app.appsets.R.string.application)
        )
    )
    tabs.add(UserAction(UserAction.ACTION_SCREEN, UserAction.ACTION_SCREEN))
    tabs.add(UserAction(UserAction.ACTION_GOODS, stringResource(xcj.app.appsets.R.string.goods)))

    tabs.add(UserAction(UserAction.ACTION_CHAT, stringResource(id = xcj.app.appsets.R.string.chat)))
    if (!LocalAccountManager.isLoggedUser(userInfo.uid) && !RelationsUseCase.getInstance()
            .hasUserRelated(userInfo.uid)
    ) {
        tabs.add(
            UserAction(
                UserAction.ACTION_ADD_FRIEND,
                stringResource(id = xcj.app.appsets.R.string.add_friend)
            )
        )
    }

    if (userInfo.uid == LocalAccountManager.userInfo.uid) {
        tabs.add(
            UserAction(
                UserAction.ACTION_UPDATE_INFO,
                stringResource(id = xcj.app.appsets.R.string.update_information)
            )
        )
    } else {
        val currentFollowState = if (isLoginUserFollowedThisUser) {
            stringResource(xcj.app.appsets.R.string.cancel_follow)
        } else {
            stringResource(xcj.app.appsets.R.string.follow)
        }
        tabs.add(UserAction(UserAction.ACTION_FLIP_FOLLOW, currentFollowState))
    }

    tabs.add(
        UserAction(
            UserAction.ACTION_FOLLOW_STATE,
            "${userFollowers.size}/${userFollowed.size} (Follower/Followed)"
        )
    )
    return tabs
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserInfoHeader(
    modifier: Modifier,
    userInfo: UserInfo,
    userFollowers: List<UserInfo>,
    userFollowed: List<UserInfo>,
    isLoginUserFollowedThisUser: Boolean,
    onActionClick: (UserAction) -> Unit,
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            AnyImage(
                modifier = Modifier
                    .size(250.dp)
                    .clip(ExtraLarge2)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = ExtraLarge2
                    ),
                model = userInfo.bioUrl,
                error = userInfo.name
            )

            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = userInfo.name ?: "",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            val introduction = if (userInfo.introduction.isNullOrEmpty()) {
                stringResource(id = xcj.app.appsets.R.string.no_introduction)
            } else {
                userInfo.introduction ?: ""
            }
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = introduction,
                fontSize = 12.sp
            )
            val actions = makeUserActions(
                userInfo,
                isLoginUserFollowedThisUser,
                userFollowers,
                userFollowed
            )
            UserActions(
                actions = actions,
                onActionClick = onActionClick
            )

            DesignHDivider()
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(ExtraLarge2)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = ExtraLarge2
                        ),
                    model = userInfo.bioUrl
                )
                Text(
                    text = userInfo.name ?: "",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userInfo.introduction
                        ?: stringResource(id = xcj.app.appsets.R.string.no_introduction)
                )
            }
            val actions = makeUserActions(
                userInfo,
                isLoginUserFollowedThisUser,
                userFollowers,
                userFollowed
            )
            UserActions(
                actions = actions,
                onActionClick = onActionClick
            )
        }
    }
}

@Composable
fun UserActions(
    actions: List<UserAction>,
    onActionClick: (UserAction) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),

        ) {
        val textModifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
        actions.forEachIndexed { index, action ->
            SuggestionChip(
                onClick = {
                    onActionClick(action)
                },
                shape = CircleShape,
                label = {
                    Text(text = action.name, modifier = textModifier, fontSize = 12.sp)
                }
            )
        }
    }
}