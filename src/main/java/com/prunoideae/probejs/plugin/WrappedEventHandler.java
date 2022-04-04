package com.prunoideae.probejs.plugin;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.IEventHandler;

import java.util.HashMap;
import java.util.Map;

public record WrappedEventHandler(String event, IEventHandler inner) implements IEventHandler {
    public static Map<String, Class<? extends EventJS>> capturedEvents = new HashMap<>();

    @Override
    public void onEvent(EventJS eventJS) {
        //Special handlers for registry events
        if (!(eventJS instanceof RegistryObjectBuilderTypes.RegistryEventJS))
            WrappedEventHandler.capturedEvents.put(this.event, eventJS.getClass());
        this.inner.onEvent(eventJS);
    }
}
