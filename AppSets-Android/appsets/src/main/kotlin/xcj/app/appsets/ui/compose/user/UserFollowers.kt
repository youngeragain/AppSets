package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ExpressivePageIndicator
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox2
import xcj.app.compose_share.components.statusBarWithTopActionBarPaddingValues

@Preview(showBackground = true)
@Composable
fun UserFollowersPreview() {
    UserFollowers(
        currentUserInfo = UserInfo.basic("", "name", ""),
        userFollowers = listOf(),
        userFollowed = listOf(),
        onBioClick = {}
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserFollowers(
    currentUserInfo: UserInfo,
    userFollowers: List<UserInfo>?,
    userFollowed: List<UserInfo>?,
    onBioClick: (Bio) -> Unit,
) {
    val pagerState = rememberPagerState { 2 }
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 12.dp
        )
        { page ->
            if (page == 0) {
                FollowersPage(userFollowers = userFollowers, onBioClick = onBioClick)
            } else {
                FollowedPage(
                    currentUserInfo = currentUserInfo,
                    userFollowed = userFollowed,
                    onBioClick = onBioClick
                )
            }
        }

        ExpressivePageIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    top = 16.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                        .calculateTopPadding()
                ),
            pagerState = pagerState
        )
    }
}

@Composable
fun FollowedPage(
    currentUserInfo: UserInfo,
    userFollowed: List<UserInfo>?,
    onBioClick: (Bio) -> Unit,
) {
    val configuration = LocalConfiguration.current
    VerticalOverscrollBox2(
        modifier = Modifier.fillMaxSize()
    ) {
        if (userFollowed.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = String.format(
                        stringResource(id = R.string.no_a),
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items = userFollowed) { userInfo ->
                    UserAvatarNameIntroductionComponent(
                        userInfo = userInfo,
                        onBioClick = onBioClick,
                        endContent = {
                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                                LocalAccountManager.userInfo.uid == currentUserInfo.uid
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
                                LocalAccountManager.userInfo.uid == currentUserInfo.uid
                            ) {
                                SuggestionChip(
                                    onClick = {

                                    },
                                    label = {
                                        Text(
                                            text = "Cancel Follow",
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        })
                }
            }
        }
    }
}

@Composable
fun FollowersPage(
    userFollowers: List<UserInfo>?,
    onBioClick: (Bio) -> Unit
) {
    VerticalOverscrollBox2(
        modifier = Modifier.fillMaxSize()
    ) {
        if (userFollowers.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = String.format(
                        stringResource(id = R.string.no_a),
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