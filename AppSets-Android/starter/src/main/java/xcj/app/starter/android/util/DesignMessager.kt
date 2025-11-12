package xcj.app.starter.android.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class DesignMessageDeliver : Handler.Callback {

    companion object {
        private const val TAG = "DesignMessageDeliver"

        private const val DELIVERY_TYPE_MAIN_THREAD = 0

        private const val DELIVERY_TYPE_OTHER_THREAD = 1
    }

    private var deliveryThreadType = DELIVERY_TYPE_OTHER_THREAD

    private var mMainThreadHandler: Handler? = null

    private var mOtherThreadHandler: Handler? = null

    private val keyedObservers: MutableList<KeyedObserver<Any, Any>> = mutableListOf()

    override fun handleMessage(message: Message): Boolean {
        val messageWrapper = message.obj as? MessageWrapper<*, *> ?: return false
        when (deliveryThreadType) {
            DELIVERY_TYPE_MAIN_THREAD -> {
                PurpleLogger.current.d(
                    TAG,
                    "delivery for key:${messageWrapper.key} on MainThread, message:$message"
                )
            }

            DELIVERY_TYPE_OTHER_THREAD -> {
                PurpleLogger.current.d(
                    TAG,
                    "delivery for key:${messageWrapper.key} on OtherThread, message:$message"
                )
            }
        }
        for (keyedObserver in keyedObservers) {
            val shouldDelivery = keyedObserver.key == messageWrapper.key
            if (shouldDelivery) {
                PurpleLogger.current.d(
                    TAG,
                    "delivery for key:${messageWrapper.key}, keyedObserver:[${keyedObserver.key},${keyedObserver.lifecycleOwnerName},${keyedObserver.lifecycleOwnerHash}]"
                )
                val content = messageWrapper.content
                if (content != null) {
                    keyedObserver.observer.onChanged(content)
                }
            }
        }
        return false
    }

    fun <K, V> post(key: K, value: V?, delayed: Long = 0L) {
        val msg = Message.obtain().apply {
            obj = MessageWrapper(key, value)
        }
        when (deliveryThreadType) {
            DELIVERY_TYPE_MAIN_THREAD -> {
                if (mMainThreadHandler == null) {
                    mMainThreadHandler = Handler(Looper.getMainLooper(), this)
                }
                mMainThreadHandler?.sendMessageDelayed(msg, delayed)
            }

            DELIVERY_TYPE_OTHER_THREAD -> {
                if (mOtherThreadHandler == null) {
                    mOtherThreadHandler = run {
                        val handlerThread = HandlerThread("DesignMessageDeliverThread")
                        handlerThread.start()
                        Handler(handlerThread.looper, this)
                    }
                }
                mOtherThreadHandler?.sendMessageDelayed(msg, delayed)
            }
        }
    }

    @Throws
    fun <K, V> observe(
        lifecycleOwner: LifecycleOwner,
        key: K,
        observer: Observer<V>,
    ) {
        PurpleLogger.current.d(TAG, "observe, key:$key, observer:$observer")
        if (Thread.currentThread() != Looper.getMainLooper().thread) {
            PurpleLogger.current.d(TAG, "observe, can't observe from non-main thread! return")
            return
        }

        storeKeyedObserver(lifecycleOwner, key, observer)
    }

    private fun <K, V> storeKeyedObserver(
        lifecycleOwner: LifecycleOwner,
        key: K,
        observer: Observer<V>,
    ) {
        PurpleLogger.current.d(TAG, "storeKeyedObserver, key:$key")
        var shouldCreateLifecycleEventObserver = true
        for (keyObserver in keyedObservers) {
            if (keyObserver.lifecycleOwnerHash == lifecycleOwner.hashCode()) {
                PurpleLogger.current.d(
                    TAG,
                    "storeKeyedObserver, key:$key's mapped LifecycleOwner is exist a LifecycleObserver! owner is:$lifecycleOwner"
                )
                shouldCreateLifecycleEventObserver = false
                break
            }
        }
        val keyObserver = KeyedObserver(
            key,
            observer,
            lifecycleOwner.javaClass.simpleName,
            lifecycleOwner.hashCode()
        )
        val anyAnyKeyedObserver = (keyObserver as? KeyedObserver<Any, Any>)

        if (anyAnyKeyedObserver == null) {
            PurpleLogger.current.d(TAG, "storeKeyedObserver, keyObserver cast failed!")
            return
        }
        keyedObservers.add(keyObserver)
        if (shouldCreateLifecycleEventObserver) {
            createLifecycleEventObserver(lifecycleOwner)
        }
    }

    private fun createLifecycleEventObserver(
        lifecycleOwner: LifecycleOwner,
    ) {
        PurpleLogger.current.d(TAG, "createLifecycleEventObserver for source:$lifecycleOwner")
        val lifecycleEventObserver = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    PurpleLogger.current.d(
                        TAG,
                        "onStateChanged, source:${source} has destroy, remove this LifecycleEventObserver and all keyedObserver!"
                    )
                    removeKeyedObserver(source)
                    source.lifecycle.removeObserver(this)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
    }


    private fun removeKeyedObserver(
        source: LifecycleOwner,
    ) {
        val iterator = keyedObservers.iterator()
        while (iterator.hasNext()) {
            val keyedObserver = iterator.next()
            if (keyedObserver.lifecycleOwnerHash == source.hashCode()) {
                PurpleLogger.current.d(
                    TAG,
                    "removeKeyedObserver for key:${keyedObserver.key}, source:${source}"
                )
                iterator.remove()
            }
        }
    }
}

data class MessageWrapper<K, V>(
    val key: K,
    val content: V,
    var used: Boolean = false,
)

data class KeyedObserver<K, V>(
    val key: K,
    val observer: Observer<V>,
    val lifecycleOwnerName: String,
    val lifecycleOwnerHash: Int,
)