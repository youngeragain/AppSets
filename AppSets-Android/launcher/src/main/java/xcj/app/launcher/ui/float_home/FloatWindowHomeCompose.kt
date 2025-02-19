package xcj.app.launcher.ui.float_home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FlotWindowCompose(onDragGesture: (Offset) -> Unit) {
    var clickTimes by remember {
        mutableIntStateOf(0)
    }
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragGesture(dragAmount)
                }
            }
            .padding(horizontal = 12.dp, vertical = 50.dp)
            .graphicsLayer {
                //renderEffect = BlurEffect(16f, 16f)
            }
    ) {
        Text(text = "Hello Click Time is:$clickTimes!", modifier = Modifier.clickable {
            clickTimes++
        })
    }
}
