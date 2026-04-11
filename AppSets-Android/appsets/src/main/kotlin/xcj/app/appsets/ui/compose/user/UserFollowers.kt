package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox2
import xcj.app.compose_share.components.statusBarWithTopActionBarPaddingValues

@Composable
fun UserFollowers(
    uid: String?,
    userFollowers: List<UserInfo>?,
    userFollowed: List<UserInfo>?,
    onBioClick: (Bio) -> Unit,
) {
    val configuration = LocalConfiguration.current
    Row(
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        VerticalOverscrollBox2(
            modifier = Modifier.weight(1f)
        ) {
            Column {

                Box(modifier = Modifier.weight(1f)) {
                    if (userFollowers.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = String.format(
                                    stringResource(id = xcj.app.appsets.R.string.no_a),
                                    "Follower"
                                ),
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = statusBarWithTopActionBarPaddingValues(bottom = 150.dp)
                        ) {
                            item {
                                Text(
                                    text = "Follower",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(items = userFollowers) { userInfo ->
                                UserAvatarNameIntroductionComponent(
                                    userInfo = userInfo,
                                    onBioClick = onBioClick,
                                    endContent = null,
                                    bottomContent = null
                                )
                            }
                        }
                    }
                }
            }
        }

        VerticalOverscrollBox2(
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Box(modifier = Modifier.weight(1f)) {
                    if (userFollowed.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = String.format(
                                    stringResource(id = xcj.app.appsets.R.string.no_a),
                                    "Followed"
                                ),
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = statusBarWithTopActionBarPaddingValues(bottom = 150.dp)
                        ) {
                            item {
                                Text(
                                    text = "Followed",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(items = userFollowed) { userInfo ->
                                UserAvatarNameIntroductionComponent(
                                    userInfo = userInfo,
                                    onBioClick = onBioClick,
                                    endContent = {
                                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                                            LocalAccountManager.userInfo.uid == uid
                                        ) {
                                            Row {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = {
                                                        Text(
                                                            text = "Cancel Follow",
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                        }
                                    },
                                    bottomContent = {
                                        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT &&
                                            LocalAccountManager.userInfo.uid == uid
                                        ) {
                                            SuggestionChip(
                                                onClick = {

                                                },
                                                label = {
                                                    Text(text = "Cancel Follow", fontSize = 12.sp)
                                                }
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAvatarNameIntroductionComponent(
    userInfo: UserInfo,
    onBioClick: (Bio) -> Unit,
    endContent: @Composable (() -> Unit)?,
    bottomContent: @Composable (() -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable {
                    onBioClick.invoke(userInfo)
                }
                .padding(6.dp))
        {
            AnyImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.large),
                model = userInfo.bioUrl
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = userInfo.bioName ?: "")
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userInfo.introduction
                        ?: stringResource(id = xcj.app.appsets.R.string.no_introduction),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            endContent?.invoke()
        }
        bottomContent?.invoke()
    }
}