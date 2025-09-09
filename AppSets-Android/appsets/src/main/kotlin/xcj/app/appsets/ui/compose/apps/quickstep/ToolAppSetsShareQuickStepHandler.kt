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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.main.navigateToAppSetsShareActivity
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHolder

class ToolAppSetsShareQuickStepHandler : QuickStepContentHandler {

    private var mQuickStepContentHolder: QuickStepContentHolder? = null

    override val name: Int = xcj.app.appsets.R.string.appsets_share

    override val description: Int = xcj.app.appsets.R.string.nearby_share

    override val category: Int = xcj.app.appsets.R.string.tools

    override fun accept(quickStepContentHolder: QuickStepContentHolder): Boolean {
        mQuickStepContentHolder = quickStepContentHolder
        return true
    }


    override fun getContent(onClick: () -> Unit): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            val context = LocalContext.current
            ToolAppSetsShareQuickStepHandlerContent(
                name = stringResource(name),
                description = stringResource(description),
                onClick = {
                    val quickStepContents = mQuickStepContentHolder?.quickStepContents?.let {
                        arrayListOf<QuickStepContent>().apply {
                            addAll(it)
                        }
                    }

                    if (quickStepContents == null) {
                        return@ToolAppSetsShareQuickStepHandlerContent
                    }
                    navigateToAppSetsShareActivity(context, mQuickStepContentHolder?.intent)
                }
            )
        }
        return contentCompose
    }

}

@Composable
fun ToolAppSetsShareQuickStepHandlerContent(
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
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_swap_calls_24),
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