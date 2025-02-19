package xcj.app.starter.android.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class MockLifecycleOwner() : LifecycleOwner {

    override val lifecycle: MockLifecycle = MockLifecycle()

    inner class MockLifecycle() : Lifecycle() {

        override val currentState: State = State.RESUMED

        override fun addObserver(observer: LifecycleObserver) {
            require(observer is DefaultLifecycleObserver) {
                "$observer must implement androidx.lifecycle.DefaultLifecycleObserver."
            }

            // Call the lifecycle methods in order and do not hold a reference to the observer.
            observer.onCreate(this@MockLifecycleOwner)
            observer.onStart(this@MockLifecycleOwner)
            observer.onResume(this@MockLifecycleOwner)
        }

        override fun removeObserver(observer: LifecycleObserver) {

        }
    }

}

object LocalMessager {
    private const val TAG = "LocalMessager"

    val designMessageDeliver = DesignMessageDeliver()

    inline fun <K, V> observe(
        viewLifecycleOwner: LifecycleOwner,
        key: K,
        crossinline observer: (V) -> Unit
    ) {
        designMessageDeliver.observe(viewLifecycleOwner, key, Observer<V> {
            observer(it)
        })
    }

    inline fun <K, V> observeEver(
        key: K,
        crossinline observer: (V) -> Unit
    ) {
        designMessageDeliver.observe(MockLifecycleOwner(), key, Observer<V> {
            observer(it)
        })
    }

    fun <K, V> post(key: K, any: V? = null, delayed: Long = 0L) {
        designMessageDeliver.post(key, any, delayed)
    }

}