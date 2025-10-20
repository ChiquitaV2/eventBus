package wtf.owen.eventbus

typealias EventHandler<Event> = (Event) -> Unit

interface IListenable {
	var enabled: Boolean

	fun toggle(state: Boolean = !enabled) {
		enabled = state
	}

	fun onEnable() {
//        EventBus.register(this)
	}

	fun onDisable() {
//        EventBus.unregister(this)
	}
}


inline fun <reified T : Event> IListenable.register(noinline handle: EventHandler<T>) {
	EventBus.register(T::class.java , Listener(this, handle))
}

inline fun <reified T : Event> IListenable.unregister(noinline handle: EventHandler<T>) {
	EventBus.unregister(T::class.java , Listener(this, handle))
}

/**
 * Listener data object containing the listenable object and the function to execute
 */
class Listener<T : Event>(val listener: IListenable, val handler: EventHandler<T>) {
    fun invoke(event: T) {
        if (listener.enabled) handler(event)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Listener<*>

        if (listener != other.listener) return false
        if (handler != other.handler) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listener.hashCode()
        result = 31 * result + handler.hashCode()
        return result
    }
}