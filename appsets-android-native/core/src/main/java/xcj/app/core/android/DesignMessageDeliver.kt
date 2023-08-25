package xcj.app.core.android

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.lang.ref.WeakReference


object DesignMessageDeliver {

    const val DELIVERY_TYPE_MAIN_THREAD = 0

    const val DELIVERY_TYPE_OTHER_THREAD = 1

    var deliveryThreadType = DELIVERY_TYPE_OTHER_THREAD

    private val mMessageCallback: Handler.Callback = getCallback()

    private var mMainThreadHandler: Handler = Handler(Looper.getMainLooper(), mMessageCallback)

    private var mOtherThreadHandler: Handler? = null

    private val keyTriples = mutableListOf<KeyTriple>()
    private val lifecycleOwnerEventObservers: MutableMap<String, LifecycleEventObserver> =
        mutableMapOf()

    init {
        initOtherHandler()
    }

    private fun initOtherHandler() {
        synchronized(this) {
            if (mOtherThreadHandler == null) {
                val myHandlerThread = HandlerThread("DesignMessageDeliverThread")
                myHandlerThread.start()
                mOtherThreadHandler = Handler(myHandlerThread.looper, getCallback())
            }
        }
    }


    private fun getCallback(): Handler.Callback {
        return Handler.Callback {
            (it.obj as? MsgWrapper<*>)?.let { msgWrapper ->
                when (deliveryThreadType) {
                    DELIVERY_TYPE_MAIN_THREAD -> {
                        Log.e("DesignMessageDeliver", "DELIVERY_TYPE_MAIN_THREAD onMessage:$it")
                    }

                    DELIVERY_TYPE_OTHER_THREAD -> {
                        Log.e("DesignMessageDeliver", "DELIVERY_TYPE_OTHER_THREAD onMessage:$it")
                    }
                }
                for (keyTriple in keyTriples) {
                    if (keyTriple.key == msgWrapper.key) {
                        val currentState =
                            keyTriple.viewLifecycleOwner.get()!!.lifecycle.currentState
                        if (currentState.isAtLeast(Lifecycle.State.CREATED) && currentState != Lifecycle.State.DESTROYED) {
                            keyTriple.observer.onChanged(msgWrapper.content)
                        }
                    }
                }
            }
            true
        }
    }

    fun <T : Any?> post(key: Any, any: T?, delayed: Long = 0L) {
        val msg = Message.obtain().apply {
            obj = MsgWrapper(key, any)
        }
        when (deliveryThreadType) {
            DELIVERY_TYPE_MAIN_THREAD -> {
                mMainThreadHandler.sendMessageDelayed(msg, delayed)
            }

            DELIVERY_TYPE_OTHER_THREAD -> {
                mOtherThreadHandler?.sendMessageDelayed(msg, delayed)
            }
        }
    }

    @Throws
    fun <T : Any> observe(key: Any, viewLifecycleOwner: LifecycleOwner, observer: Observer<T?>) {
        if (Thread.currentThread() != Looper.getMainLooper().thread)
            throw Exception("can't observe from non-main thread!")
        val result =
            putKeyTriplesIfAbsent(keyTriples, key, viewLifecycleOwner, observer)
        if (result) {
            addEventObserverIfNotAdd(viewLifecycleOwner, lifecycleOwnerEventObservers)
        }
    }

    private fun addEventObserverIfNotAdd(
        viewLifecycleOwner: LifecycleOwner,
        lifecycleEventObservers1: MutableMap<String, LifecycleEventObserver>
    ) {
        val key = viewLifecycleOwner.toString()
        if (lifecycleEventObservers1.containsKey(key)) {
            return
        }
        val lifecycleEventObserver = LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                removeKeyTriples(keyTriples, source)
                lifecycleEventObservers1.remove(source.toString())
            }
        }
        lifecycleEventObservers1[key] = lifecycleEventObserver
        viewLifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
    }


    private fun removeKeyTriples(keyTriples1: MutableList<KeyTriple>, source: LifecycleOwner) {
        val iterator = keyTriples1.iterator()
        while (iterator.hasNext()) {
            val keyTriple = iterator.next()
            if (keyTriple.viewLifecycleOwner.get() == source) {
                iterator.remove()
            }
        }
    }

    private fun <T : Any?> putKeyTriplesIfAbsent(
        keyTriples1: MutableList<KeyTriple>,
        key: Any,
        viewLifecycleOwner: LifecycleOwner,
        observer: Observer<T?>
    ): Boolean {
        for (keyTriple in keyTriples1) {
            if (keyTriple.key == key && keyTriple.viewLifecycleOwner.get() == viewLifecycleOwner && keyTriple.observer == observer) {
                return false
            }
        }
        keyTriples1.add(
            KeyTriple(
                key,
                WeakReference(viewLifecycleOwner),
                observer as Observer<Any?>
            )
        )
        return true
    }

    data class MsgWrapper<T : Any?>(val key: Any, val content: T?) {
        var used: Boolean = false
    }

    data class KeyTriple(
        val key: Any,
        val viewLifecycleOwner: WeakReference<LifecycleOwner>,
        val observer: Observer<Any?>
    )

}