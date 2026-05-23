package xcj.app.bard

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.video.VideoFrameDecoder
import xcj.app.starter.android.DesignApplication

class App : DesignApplication(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
