package wtf.owen.eventbus

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.get

/**
 * EventBus used to send events to registered listeners.
 * In a publish/subscribe model, events are published to the event bus and listeners are subscribed to the event bus.
 */
object EventBus {

	/**
	 * Map of event types to listeners.
	 */
	private val listeners: MutableMap<Class<out Event>, CopyOnWriteArraySet<Listener<in Event>>?> = ConcurrentHashMap()

	/**
	 * Subscribes the given listener to the given event type.
	 * @param eventType The event type to subscribe to.
	 * @param listener The listener to subscribe.
	 */
	fun <T : Event> register(event: Class<out T>, listener: Listener<T>) {
		listeners.computeIfAbsent(event) { CopyOnWriteArraySet() }?.add(listener as Listener<in Event>)

	}

	/**
	 * Unsubscribes the given listener from the given event type.
	 * @param eventType The event type to subscribe to.
	 * @param listener The listener to subscribe.
	 */
	fun <T : Event> unregister(event: Class<out T>, listener: Listener<T>) {
		listeners.computeIfAbsent(event) { CopyOnWriteArraySet() }?.remove(listener as Listener<in Event>)
	}

	/**
	 * Publishes the given event to all listeners.
	 * @param event The event to publish.
	 */
	fun <T : Event> post(event: T) {
		val invokedListeners = mutableSetOf<Listener<in Event>>()
		var eventClass: Class<*> = event.javaClass
		while (eventClass != Event::class.java) {
			listeners[eventClass]?.forEach {
				if (invokedListeners.add(it)) {
					it.invoke(event)
				}
			}
			eventClass = eventClass.superclass
		}
	}
}