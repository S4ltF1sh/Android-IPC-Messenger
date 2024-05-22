package com.s4ltf1sh.messenger_motherapp

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object SimpleEventBus {
    private val listeners = ConcurrentHashMap<Class<*>, MutableList<(Event) -> Unit>>()

    fun subscribe(eventType: Class<Event>, listener: (Event) -> Unit) {
        val eventListeners = listeners.getOrPut(eventType) { Collections.synchronizedList(mutableListOf()) }
        synchronized(eventListeners) {
            eventListeners.add(listener)
        }
    }

    fun unsubscribe(eventType: Class<Event>, listener: (Event) -> Unit) {
        listeners[eventType]?.let { eventListeners ->
            synchronized(eventListeners) {
                eventListeners.remove(listener)
            }
        }
    }

    fun post(event: Event) {
        listeners[event::class.java]?.forEach { it.invoke(event) }
    }
}

data class Event(val message: String)