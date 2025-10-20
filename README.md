# EventBus

A simple, lightweight, and thread-safe event bus implementation in Kotlin.

## Features

*   **Lightweight:** The event bus is implemented in a single file with no external dependencies.
*   **Thread-safe:** The event bus is designed to be used in a multi-threaded environment.
*   **Event Inheritance:** Listeners for superclass events will also receive subclass events.

## Usage

Here's a simple example of how to use the event bus:

### Add the EventBus to your project
```gradle
repositories {
    maven {
        name "quantumRepositorySnapshots"
        url "https://maven.quantumdev.org/snapshots"
    }
}
dependencies { 
    implementation "wtf.owen:eventbus:1.0-SNAPSHOT"
}
```
<details>
  <summary>Create and register listeners Example</summary>

```kotlin
// Define an event
class MyEvent(val message: String) : Event()

// Create a listener
class MyListener : IListenable {
    override var enabled = true

    init {
        register<MyEvent> { event ->
            println("Received event: ${event.message}")
        }
    }
// Or save as a variable
    val onEvent = register<MyEvent> { event ->
	    println("Received event: ${event.message}")
    }
}

// Create a listener instance
val listener = MyListener()

// Post an event
EventBus.post(MyEvent("Hello, World!"))
```
</details>


## Building and Testing

To build and test the project, you can use the following Gradle commands:

```bash
# Build the project
./gradlew build

# Run the tests
./gradlew test
```

## Origins 
This eventbus was originally developed for use with private Minecraft mods, now it's available for general use.
