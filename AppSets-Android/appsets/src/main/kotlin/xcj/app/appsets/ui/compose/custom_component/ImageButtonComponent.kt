package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun ImageButtonComponent(
    modifier: Modifier = Modifier,
    useImage: Boolean = true,
    resource: Any?,
    resRotate: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    if (resource == null) {
        val overrideModifier = if (onClick == null) {
            modifier
                .size(42.dp)
                .clip(CircleShape)
        } else {
            modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        }
        Box(modifier = overrideModifier)
    } else {
        val overrideModifier = if (onClick == null) {
            modifier
                .size(42.dp)
                .background(
                    MaterialTheme.colorScheme.outline,
                    CircleShape
                )
                .clip(CircleShape)
        } else {
            modifier
                .size(42.dp)
                .background(
                    MaterialTheme.colorScheme.outline,
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = onClick)
        }
        Box(
            modifier = overrideModifier,
            contentAlignment = Alignment.Center
        ) {
            if (useImage) {
                if (resource is Int) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = resource),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(resRotate),
                        contentDescription = null
                    )
                } else {
                    AnyImage(
                        any = resource,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .rotate(resRotate)
                    )
                }
            } else {
                if (resource is Int) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = resource),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(resRotate),
                        contentDescription = null
                    )
                } else {
                    AnyImage(
                        any = resource,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .rotate(resRotate)
                    )
                }
            }
        }
    }
}

