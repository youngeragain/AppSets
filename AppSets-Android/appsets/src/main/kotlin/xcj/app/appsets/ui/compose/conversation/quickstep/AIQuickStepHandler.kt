package xcj.app.appsets.ui.compose.conversation.quickstep

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import xcj.app.appsets.R
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.main.navigateWithBundle
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHolder

class AIQuickStepHandler(context: Context) : QuickStepContentHandler(context) {

    private var mQuickStepContentHolder: QuickStepContentHolder? = null

    override fun getName(): String {
        return context.getString(R.string.ai)
    }

    override fun getDescription(): String {
        return context.getString(R.string.get_suggestions)
    }

    override fun getCategory(): String {
        return context.getString(R.string.tools)
    }

    override fun accept(quickStepContentHolder: QuickStepContentHolder): Boolean {
        mQuickStepContentHolder = quickStepContentHolder
        return true
    }

    override fun getContent(onClick: () -> Unit): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            val navController = LocalNavHostController.current
            AIQuickStepHandlerContent(
                name = getName(),
                description = getDescription(),
                onClick = {
                    val quickStepContents = mQuickStepContentHolder?.quickStepContents?.let {
                        arrayListOf<QuickStepContent>().apply {
                            addAll(it)
                        }
                    }
                    if (quickStepContents == null) {
                        return@AIQuickStepHandlerContent
                    }
                    navigateWithBundle(
                        navController,
                        PageRouteNames.ConversationOverviewPage,
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
private fun AIQuickStepHandlerContent(
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
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_stars_2_24),
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