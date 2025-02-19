package xcj.app.appsets.ui.compose.media.video.fall

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import xcj.app.appsets.ui.base.InfinityPagerAdapter

class MediaFallPagerAdapter(activity: FragmentActivity) :
    InfinityPagerAdapter<MediaFallFragment>(activity) {

    override fun createFragment(position: Int): Fragment {
        return MediaFallFragment().apply {
            arguments = Bundle().apply {
                putInt(REAL_POSITION, position)
            }
        }
    }
}