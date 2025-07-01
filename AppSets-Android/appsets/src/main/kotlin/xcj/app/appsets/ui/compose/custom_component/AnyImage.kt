package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun AnyImage(
    modifier: Modifier = Modifier,
    any: Any?,
    defaultColor: Color = Color.Transparent,
    contentScale: ContentScale = ContentScale.Crop,
    error: Any? = null,
) {
    val placeHolderOrErrorPainter = error?.let { anyPainter(it) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(any)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
        error = placeHolderOrErrorPainter
    )
}