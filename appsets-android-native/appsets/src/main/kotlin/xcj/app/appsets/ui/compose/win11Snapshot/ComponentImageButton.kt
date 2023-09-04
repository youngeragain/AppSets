package xcj.app.appsets.ui.compose.win11Snapshot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import xcj.app.appsets.R

@Composable
fun ComponentImageButton(
    modifier: Modifier,
    useImage: Boolean = true,
    resId: Int = R.drawable.ic_plus,
    onClick: () -> Unit
) {
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(14.dp)
                )
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (useImage) {
                Image(
                    imageVector = ImageVector.vectorResource(id = resId),
                    modifier = Modifier.size(22.dp),
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(id = resId),
                    modifier = Modifier.size(22.dp),
                    contentDescription = null
                )
            }

        }
    }
}

