package com.prunoideae.probejs.plugin;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.IEventHandler;

import java.util.HashMap;
import java.util.Map;

public record WrappedEventHandler(String event, IEventHandler inner) implements IEventHandler {
    public static Map<String, Class<? extends EventJS>> capturedEvents = new HashMap<>();

    @Override
    public void onEvent(EventJS eventJS) {
        WrappedEventHandler.capturedEvents.put(this.event, eventJS.getClass());
        this.inner.onEvent(eventJS);
    }
}
