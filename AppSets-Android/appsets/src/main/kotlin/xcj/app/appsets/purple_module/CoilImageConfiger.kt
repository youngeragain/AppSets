package xcj.app.appsets.purple_module

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.CachePolicy
import coil3.size.Precision
import coil3.video.VideoFrameDecoder

fun configCoil(context: Context) {
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(VideoFrameDecoder.Factory())
            }
            .diskCachePolicy(CachePolicy.DISABLED)
            .precision(Precision.INEXACT)
            .build()
    }
}