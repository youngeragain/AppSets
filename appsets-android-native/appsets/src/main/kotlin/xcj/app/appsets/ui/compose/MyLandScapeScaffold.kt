package xcj.app.appsets.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyLandScapeScaffold(appbarDirection: Direction = Direction.START,
                        appBar: @Composable ()->Unit,
                        content: @Composable ()->Unit){
    Row {
        if (appbarDirection == Direction.START ||
            appbarDirection == Direction.LEFT
        ) {
            appBar()
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        if (appbarDirection == Direction.END || appbarDirection == Direction.RIGHT) {
            appBar()
        }
    }
}