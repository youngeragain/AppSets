package xcj.app.appsets.ui.compose.apps.quickstep

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
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE_AppSets_Transform
import xcj.app.appsets.ui.compose.main.navigateWithBundle
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.compose_share.R

class ToolContentTransformQuickStepHandler : QuickStepContentHandler {

    private var mQuickStepContents: List<QuickStepContent>? = null

    override fun getName(): String {
        return "Transform"
    }

    override fun getCategory(): String {
        return "Tool"
    }

    override fun accept(contents: List<QuickStepContent>): Boolean {
        val firstTextQuickStepContent = contents.firstOrNull { it is TextQuickStepContent }
        val accept = firstTextQuickStepContent != null
        if (accept) {
            mQuickStepContents = contents
        }
        return accept
    }


    override fun getContent(onClick: () -> Unit): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            val navController = LocalNavHostController.current
            ToolContentTransformQuickStepHandlerContent(
                name = getName(),
                onClick = {
                    val quickStepContents = mQuickStepContents?.let {
                        arrayListOf<QuickStepContent>().apply {
                            addAll(it)
                        }
                    }

                    if (quickStepContents == null) {
                        return@ToolContentTransformQuickStepHandlerContent
                    }
                    navigateWithBundle(
                        navController,
                        PageRouteNames.AppToolsDetailsPage,
                        bundleCreator = {
                            bundleOf().apply {
                                putString(TOOL_TYPE, TOOL_TYPE_AppSets_Transform)
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
fun ToolContentTransformQuickStepHandlerContent(
    name: String,
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
                    painter = painterResource(R.drawable.ic_outline_qr_code_24),
                    contentDescription = null
                )
                Column {
                    Text(text = name, fontSize = 12.sp)
                    Text(text = "Content Transform", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.widthIn(12.dp))
            }
        }
    }
}