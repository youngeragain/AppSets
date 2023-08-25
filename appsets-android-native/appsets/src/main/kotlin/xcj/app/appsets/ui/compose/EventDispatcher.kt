package xcj.app.appsets.ui.compose

object EventDispatcher {
    private var eventReceivers: MutableMap<String, EventReceiver>? = null
    fun addEventReceiver(eventReceiver: EventReceiver) {
        if (eventReceivers == null)
            eventReceivers = mutableMapOf()
        eventReceivers?.put(eventReceiver.getKey(), eventReceiver)
    }

    fun removeEventReceiver(eventReceiver: EventReceiver) {
        eventReceivers?.remove(eventReceiver.getKey())
    }

    fun dispatchEvent(event: Event, key: String? = null) {
        if (key != null)
            eventReceivers?.get(key)?.onEvent(event)
        else {
            eventReceivers?.forEach {
                it.value.onEvent(event)
            }
        }
    }
}

abstract class Event {
    var payload: Any? = null
}

class DataSyncFinishEvent : Event()
interface EventReceiver {
    fun getKey(): String
    fun onEvent(e: Event)
}

