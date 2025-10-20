package wtf.owen.eventbus

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class EventBusTest {

    @BeforeTest
    fun setUp() {
        // Reset the EventBus before each test
        val listenersField = EventBus.javaClass.getDeclaredField("listeners")
        listenersField.isAccessible = true
        val listeners = listenersField.get(EventBus) as MutableMap<*, *>
        listeners.clear()
    }

    // Basic event firing test
    @Test
    fun `test single event listener`() {
        var eventHandled = false
        val listener = object : IListenable {
            override var enabled = true
        }
        listener.register<AEvent> {
            eventHandled = true
        }
        EventBus.post(AEvent("test"))
        assertTrue("Event should have been handled", eventHandled)
    }

    // Test for event data integrity
    @Test
    fun `test event data integrity`() {
        val testData = "Hello, World!"
        var receivedData: String? = null
        val listener = object : IListenable {
            override var enabled = true
        }
        listener.register<AEvent> {
            receivedData = it.data
        }
        EventBus.post(AEvent(testData))
        assertEquals(testData, receivedData, "Received data should match sent data")
    }

    // Test multiple listeners for the same event
    @Test
    fun `test multiple listeners for same event`() {
        val executionCount = AtomicInteger(0)
        val listener1 = object : IListenable {
            override var enabled = true
        }
        val listener2 = object : IListenable {
            override var enabled = true
        }

        listener1.register<AEvent> { executionCount.incrementAndGet() }
        listener2.register<AEvent> { executionCount.incrementAndGet() }

        EventBus.post(AEvent("test"))
        assertEquals( 2, executionCount.get(), "Both listeners should have been executed")
    }

    // Test unregistering a listener
    @Test
    fun `test unregister listener`() {
        var eventHandled = false
        val listener = object : IListenable {
            override var enabled = true
        }
        val handler: EventHandler<AEvent> = { eventHandled = true }

        listener.register(handler)
        EventBus.post(AEvent("first"))
        assertTrue("Event should be handled before unregistering", eventHandled)

        eventHandled = false
        listener.unregister(handler)
        EventBus.post(AEvent("second"))
        assertFalse(eventHandled, "Event should not be handled after unregistering")
    }

    // Thread safety test
    @Test
    fun `test thread safety with multiple threads`() {
        val numberOfThreads = 100
        val eventsPerThread = 1000
        val executionCount = AtomicInteger(0)
        val latch = CountDownLatch(numberOfThreads)

        val listener = object : IListenable {
            override var enabled = true
        }
        listener.register<AEvent> {
            executionCount.incrementAndGet()
        }

        val executor = Executors.newFixedThreadPool(numberOfThreads)
        repeat(numberOfThreads) {
            executor.submit {
                repeat(eventsPerThread) {
                    EventBus.post(AEvent("event $it"))
                }
                latch.countDown()
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        val expectedCount = numberOfThreads * eventsPerThread
        assertEquals(expectedCount, executionCount.get(), "All events from all threads should be handled")
    }

    // Event inheritance test
    open class SuperEvent : Event()
    class SubEvent : SuperEvent()

    @Test
    fun `test event inheritance`() {
        var superEventHandled = false
        val listener = object : IListenable {
            override var enabled = true
        }
        listener.register<SuperEvent> {
            superEventHandled = true
        }
        EventBus.post(SubEvent())
        assertTrue("Listener for superclass should handle subclass event", superEventHandled)
    }

    // Test posting an event with no listeners
    @Test
    fun `test post event with no listeners`() {
        try {
            EventBus.post(AEvent("unheard"))
            // No exception should be thrown
        } catch (e: Exception) {
            fail("Posting an event with no listeners should not throw an exception")
        }
    }

    // Test disabled listener
    @Test
    fun `test disabled listener`() {
        var eventHandled = false
        val listener = object : IListenable {
            override var enabled = false // Disabled
        }
        listener.register<AEvent> {
            eventHandled = true
        }
        EventBus.post(AEvent("test"))
        assertFalse(eventHandled, "Disabled listener should not handle event")
    }

    @Test
    fun `test max capacity with many listeners and events`() {
        val numListeners = 1000
        val numEvents = 10000
        val numThreads = 16

        val executionCount = LongAdder()

        // Register a large number of listeners
        repeat(numListeners) {
            val listener = object : IListenable {
                override var enabled = true
            }
            listener.register<AEvent> {
                executionCount.increment()
            }
        }

        val executor = Executors.newFixedThreadPool(numThreads)
        val startTime = System.currentTimeMillis()

        val latch = CountDownLatch(numEvents)
        // Post a large number of events from multiple threads
        repeat(numEvents) {
            executor.submit {
                EventBus.post(AEvent("event $it"))
                latch.countDown()
            }
        }

        latch.await(60, TimeUnit.SECONDS)
        executor.shutdown()

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime

		println("Handled $numEvents events with $numListeners listeners in $totalTime ms")
		println("Throughput: ${numEvents / (totalTime / 1000.0)} events/sec")

        assertEquals( (numListeners * numEvents).toLong(), executionCount.sum(), "All events should be handled by all listeners")
    }
}

class AEvent(val data: String) : Event()