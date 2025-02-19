package xcj.app.appsets.ui.compose.ime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IMEMainContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        Text("ime")
    }
}