package xcj.app.appsets.ui.compose

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ui.compose.win11Snapshot.anyToPainter
import java.io.File

@Composable
fun LocalOrRemoteImage(
    modifier: Modifier = Modifier,
    any: Any?,
    defaultColor: Color = MaterialTheme.colorScheme.primary,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (any.isHttpUrl() || (any is Uri) || (any is File)) {
        AsyncImage(
            model = any,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        val painter = anyToPainter(any = any, defaultColor)
        Image(
            contentScale = contentScale,
            painter = painter,
            contentDescription = null,
            modifier = modifier
        )
    }
}