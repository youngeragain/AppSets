package xcj.app.appsets.ui.compose.custom_component

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import xcj.app.starter.android.ktx.startWithHttpSchema
import java.io.File

@Composable
fun AnyImage(
    modifier: Modifier = Modifier,
    any: Any?,
    defaultColor: Color = Color.Transparent,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (any is String) {
        if (any.startWithHttpSchema()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(any)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier
            )
        }

    } else if ((any is Uri) ||
        (any is File) ||
        (any is ByteArray) ||
        (any is Int) ||
        (any is Drawable) ||
        (any is Bitmap)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(any)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            contentScale = contentScale,
            painter = anyPainter(any = any, defaultColor),
            contentDescription = null,
            modifier = modifier
        )
    }
}