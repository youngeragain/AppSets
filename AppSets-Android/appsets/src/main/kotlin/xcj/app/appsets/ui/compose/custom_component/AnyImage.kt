package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun AnyImage(
    modifier: Modifier = Modifier,
    model: Any?,
    placeHolder: Any? = null,
    error: Any? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeHolderPainter = placeHolder?.let { anyPainter(it) }
    val errorPainter = error?.let { anyPainter(it) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
        placeholder = placeHolderPainter,
        error = errorPainter
    )
}