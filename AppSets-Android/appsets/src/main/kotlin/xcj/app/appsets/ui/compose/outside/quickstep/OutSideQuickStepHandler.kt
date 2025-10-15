package xcj.app.appsets.ui.compose.outside.quickstep

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.core.os.bundleOf
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.main.navigateWithBundle
import xcj.app.appsets.ui.compose.quickstep.HandlerClickParams
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHolder
import xcj.app.appsets.ui.compose.quickstep.QuickStepInfo
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.quickstep.UriQuickStepContent
import xcj.app.starter.util.ContentType

class OutSideQuickStepHandler : QuickStepContentHandler() {

    private var mQuickStepContentHolder: QuickStepContentHolder? = null

    override val quickStepInfo: QuickStepInfo = QuickStepInfo(
        xcj.app.appsets.R.string.out_side,
        xcj.app.appsets.R.string.create_screen,
        xcj.app.appsets.R.string.social
    )

    override fun canAccept(quickStepContentHolder: QuickStepContentHolder): Boolean {
        var accept = false
        for (content in quickStepContentHolder.quickStepContents) {
            if (content is TextQuickStepContent) {
                accept = true
                break
            }
            if (content is UriQuickStepContent) {
                if (ContentType.isImage(content.uriContentType)) {
                    accept = true
                    break
                } else if (ContentType.isVideo(content.uriContentType)) {
                    accept = true
                    break
                }
            }
        }
        if (accept) {
            mQuickStepContentHolder = quickStepContentHolder
        }
        return accept
    }

    override fun getContent(onClick: (HandlerClickParams) -> Unit): @Composable () -> Unit {
        val contentCompose = @Composable {
            val navController = LocalNavHostController.current
            OutSideQuickStepHandlerContent(
                name = stringResource(quickStepInfo.name),
                description = stringResource(quickStepInfo.description),
                onClick = {
                    onClick(HandlerClickParams.SimpleClick)
                    val quickStepContents = mQuickStepContentHolder?.quickStepContents?.let {
                        arrayListOf<QuickStepContent>().apply {
                            addAll(it)
                        }
                    }
                    if (quickStepContents == null) {
                        return@OutSideQuickStepHandlerContent
                    }
                    navController.navigateWithBundle(
                        PageRouteNames.CreateScreenPage,
                        bundleCreator = {
                            bundleOf().apply {
                                putParcelableArrayList(
                                    Constants.QUICK_STEP_CONTENT,
                                    quickStepContents
                                )
                            }
                        }
                    )
                }
            )
        }
        return contentCompose
    }

}

@Composable
fun OutSideQuickStepHandlerContent(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(max = 180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_explore_24),
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