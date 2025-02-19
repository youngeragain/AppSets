package xcj.app.appsets.ui.compose.media.video.fall

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.viewpager2.widget.ViewPager2

@Composable
fun MediaFallContent() {
    val viewModel = viewModel<MediaFallViewModel>()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AndroidView(
            factory = {
                val fragmentActivity = it as FragmentActivity
                val viewPager2 = ViewPager2(it)
                viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
                val infinityPagerAdapter = MediaFallPagerAdapter(fragmentActivity)
                viewPager2.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        val mediaFallFragment =
                            fragmentActivity.supportFragmentManager.findFragmentByTag("f$position") as? MediaFallFragment
                        viewModel.updateCurrentPagerPosition(mediaFallFragment, position)
                    }
                })
                viewPager2.adapter = infinityPagerAdapter
                viewPager2.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                //viewPager2.setPageTransformer(ZoomOutTransformation())
                viewPager2
            }, modifier = Modifier.fillMaxSize()
        )
    }
}