package xcj.app.screen_share.ui.compose.standard_home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

sealed interface WindowHomeBackgroundState {
    @Composable
    fun Content()

    data class Color(val color: Int? = null) :
        WindowHomeBackgroundState {

        @Composable
        override fun Content() {
            if (color != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(color))
                ) {

                }
            }
        }
    }

    data class LocalImage(val imageRes: Int) :
        WindowHomeBackgroundState {

        @Composable
        override fun Content() {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp),
                contentScale = ContentScale.Crop
            )
        }
    }

    data class RemoteImage(val imageUri: String) :
        WindowHomeBackgroundState {

        @Composable
        override fun Content() {
            AsyncImage(
                model = imageUri,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
