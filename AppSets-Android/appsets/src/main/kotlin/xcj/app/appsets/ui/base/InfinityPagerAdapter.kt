package xcj.app.appsets.ui.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class InfinityPagerAdapter<F : Fragment>(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val REAL_POSITION = "real_position"
        const val LOGIC_POSITION = "logic_position"
    }

    override fun getItemCount(): Int {
        return Short.MAX_VALUE.toInt()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return true
    }
}