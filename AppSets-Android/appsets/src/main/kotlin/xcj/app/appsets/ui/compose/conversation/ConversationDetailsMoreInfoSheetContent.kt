package xcj.app.appsets.ui.compose.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.GenerativeAISessions
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.usecase.RelationsUseCase

@Preview(showBackground = true)
@Composable
fun ConversationDetailsMoreInfoSheetContentPreView() {
    //ConversationDetailsMoreInfoSheetContent({ })
}

@Composable
fun ConversationDetailsMoreInfoSheetContent(
    imObj: ImObj,
    onBioClick: (Bio) -> Unit,
    onRequestAddFriend: (String) -> Unit,
    onRequestDeleteFriend: (String) -> Unit,
    onRequestJoinGroup: (String) -> Unit,
    onRequestLeaveGroup: (String) -> Unit,
    onRequestDeleteGroup: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (imObj.bio is GenerativeAISessions.AIBio) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.only_for_learning_purpose),
                    fontSize = 12.sp,
                )
            }
        }
        if (imObj is ImObj.ImSingle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onBioClick(imObj.bio)
                    }
                    .padding(12.dp)) {
                Text(text = stringResource(xcj.app.appsets.R.string.check_the_details))
            }
            if (imObj.bio !is GenerativeAISessions.AIBio) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        if (RelationsUseCase.getInstance().hasUserRelated(imObj.id)) {
                            onRequestDeleteFriend(imObj.id)
                        } else {
                            onRequestAddFriend(imObj.id)
                        }
                    }
                    .padding(12.dp))
                {
                    val textRes = if (RelationsUseCase.getInstance().hasUserRelated(imObj.id)) {
                        xcj.app.appsets.R.string.dissolve_the_relationship
                    } else {
                        xcj.app.appsets.R.string.add_friend
                    }
                    Text(text = stringResource(textRes))
                }
            }

        } else if (imObj is ImObj.ImGroup) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    onBioClick(imObj.bio)
                }
                .padding(12.dp))
            {
                Text(text = stringResource(xcj.app.appsets.R.string.check_the_details))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    if (RelationsUseCase.getInstance().hasGroupRelated(imObj.id)) {
                        onRequestLeaveGroup(imObj.id)
                    } else {
                        onRequestJoinGroup(imObj.id)
                    }

                }
                .padding(12.dp))
            {
                val textRes = if (RelationsUseCase.getInstance().hasGroupRelated(imObj.id)) {
                    xcj.app.appsets.R.string.dissolve_the_relationship
                } else {
                    xcj.app.appsets.R.string.apply_to_join
                }
                Text(text = stringResource(textRes))
            }
        }
    }
}

