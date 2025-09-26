package xcj.app.appsets.ui.compose.conversation.quickstep

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.quickstep.HandlerClickParams
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHolder

class ConversationQuickStepHandler : QuickStepContentHandler() {

    private var mQuickStepContentHolder: QuickStepContentHolder? = null

    override val name: Int = xcj.app.appsets.R.string.conversation

    override val description: Int = xcj.app.appsets.R.string.send_to_people

    override val category: Int = xcj.app.appsets.R.string.social

    override fun canAccept(quickStepContentHolder: QuickStepContentHolder): Boolean {
        mQuickStepContentHolder = quickStepContentHolder
        return true
    }

    override fun getContent(onClick: (HandlerClickParams) -> Unit): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            val navController = LocalNavHostController.current
            ConversationQuickStepHandlerContent(
                name = stringResource(name),
                description = stringResource(description),
                onClick = {

                    val quickStepContents = mQuickStepContentHolder?.quickStepContents?.let {
                        arrayListOf<QuickStepContent>().apply {
                            addAll(it)
                        }
                    }
                    if (quickStepContents == null) {
                        return@ConversationQuickStepHandlerContent
                    }
                    /*navController.navigateWithBundle(
                        PageRouteNames.ConversationOverviewPage,
                        bundleCreator = {
                            bundleOf().apply {
                                putParcelableArrayList(
                                    Constants.QUICK_STEP_CONTENT,
                                    quickStepContents
                                )
                            }
                        }
                    )*/
                    val requestReplaceHostContent = HandlerClickParams.RequestReplaceHostContent(
                        "quick_send_content",
                        quickStepContents
                    )
                    onClick(requestReplaceHostContent)
                }
            )
        }
        return contentCompose
    }

    override fun getHostReplaceContent(requestReplaceHostContent: HandlerClickParams.RequestReplaceHostContent): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            Box {
                if (requestReplaceHostContent.replaceRequest == "quick_send_content") {
                    val quickStepContents =
                        requestReplaceHostContent.payload as? List<QuickStepContent>
                    ConversationQuickStepBottomSheet(quickStepContents)
                }
            }
        }
        return contentCompose
    }

}

@Composable
private fun ConversationQuickStepHandlerContent(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(max = 180.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .heightIn(min = 82.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_bubble_chart_24),
                    contentDescription = null
                )
                Column {
                    Text(text = name, fontSize = 12.sp)
                    Text(
                        text = description,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.widthIn(12.dp))
            }
        }
    }
}