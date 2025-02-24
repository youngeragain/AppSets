package xcj.app.appsets.ui.compose.outside.quickstep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandler

class OutSideQuickStepHandler : QuickStepContentHandler {
    override fun getName(): String {
        return "Out side"
    }

    override fun getCategory(): String {
        return "Default"
    }

    override fun accept(contentTypes: List<String>): Boolean {
        return true
    }

    override fun handleContent(content: Any) {

    }

    override fun getContent(): @Composable (() -> Unit) {
        val contentCompose = @Composable {
            OutSideQuickStepHandlerContent(name = getName())
        }
        return contentCompose
    }

}

@Composable
fun OutSideQuickStepHandlerContent(name: String) {
    Column(
        modifier = Modifier.widthIn(max = 180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.heightIn(min = 82.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_explore_24),
                    contentDescription = null
                )
                Column {
                    Text(text = name, fontSize = 12.sp)
                    Text(text = "Create Screen", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.widthIn(12.dp))
            }
        }
    }
}