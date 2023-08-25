package xcj.app.appsets.ktx

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

inline fun <reified F: Fragment> AppCompatActivity.findFragment(): F? {
    val fragments = supportFragmentManager.fragments
    if (fragments.size == 0)
        return null
    val navHostFragment = (fragments[0] as? NavHostFragment)?:return null
    val childFragments = navHostFragment.childFragmentManager.fragments
    val size = childFragments.size
    if (size == 0)
        return null
    for (j in 0 until size) {
        val fragment = childFragments[j]
        if (fragment.javaClass.isAssignableFrom(F::class.java))
            return fragment as? F
    }
    return null
}