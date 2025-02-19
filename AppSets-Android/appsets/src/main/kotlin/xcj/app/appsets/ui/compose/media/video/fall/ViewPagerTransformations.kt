package xcj.app.appsets.ui.compose.media.video.fall

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class CubeOutScalingTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1) {    // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.pivotX = page.width.toFloat()
            page.rotationY = -90 * abs(position)

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.pivotX = 0f
            page.rotationY = 90 * abs(position)

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }

        if (abs(position) <= 0.5) {
            page.scaleY = 0.4f.coerceAtLeast(1 - abs(position))
        } else if (abs(position) <= 1) {
            page.scaleY = 0.4f.coerceAtLeast(abs(position))
        }
    }

}

class CubeOutScalingTransformationVertical : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1) {    // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.pivotY = page.height.toFloat()
            page.rotationX = 90 * abs(position)

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.pivotY = 0f
            page.rotationX = -90 * abs(position)

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }

        if (abs(position) <= 0.5) {
            page.scaleY = 0.4f.coerceAtLeast(1 - abs(position))
        } else if (abs(position) <= 1) {
            page.scaleY = 0.4f.coerceAtLeast(abs(position))
        }
    }

}

class CubeOutRotationTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1) {    // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.pivotX = page.width.toFloat()
            page.rotationY = -90 * abs(position)

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.pivotX = 0f;
            page.rotationY = 90 * abs(position)

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }
    }
}

class PopTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.translationY = -position * page.height

        if (abs(position) < 0.5) {
            page.visibility = View.VISIBLE
            page.scaleX = 1 - abs(position)
            page.scaleY = 1 - abs(position)
        } else if (abs(position) > 0.5) {
            page.visibility = View.GONE
        }
    }
}

class VerticalShutTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.translationX = -position * page.width
        page.setCameraDistance(999999999f)

        if (position < 0.5 && position > -0.5) {
            page.visibility = View.VISIBLE;
        } else {
            page.visibility = View.INVISIBLE;
        }

        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.rotationX = 180 * (1 - abs(position) + 1)

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.rotationX = -180 * (1 - abs(position) + 1);

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }
    }
}

class TossTransformation : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {

        page.translationX = -position * page.width
        page.setCameraDistance(20000f)


        if (position < 0.5 && position > -0.5) {
            page.visibility = View.VISIBLE

        } else {
            page.visibility = View.INVISIBLE

        }

        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.scaleX = 0.4f.coerceAtLeast((1 - abs(position)))
            page.scaleY = 0.4f.coerceAtLeast((1 - abs(position)))
            page.rotationX = 1080 * (1 - abs(position) + 1)
            page.translationY = -1000 * abs(position)

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.scaleX = 0.4f.coerceAtLeast((1 - abs(position)))
            page.scaleY = 0.4f.coerceAtLeast((1 - abs(position)))
            page.rotationX = -1080 * (1 - abs(position) + 1)
            page.translationY = -1000 * abs(position)

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }
    }
}

class ZoomOutTransformation : ViewPager2.PageTransformer {


    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.05f
    }

    override fun transformPage(page: View, position: Float) {

        if (position < -1) {  // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)

        } else if (position <= 1) { // [-1,1]
            page.scaleX = MIN_SCALE.coerceAtLeast(1 - abs(position))
            page.scaleY = MIN_SCALE.coerceAtLeast(1 - abs(position))
            page.setAlpha(MIN_ALPHA.coerceAtLeast(1 - abs(position)))

        } else {  // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)

        }
    }
}
