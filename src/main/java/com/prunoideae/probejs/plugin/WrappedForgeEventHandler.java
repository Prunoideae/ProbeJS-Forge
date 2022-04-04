package com.prunoideae.probejs.plugin;

import dev.latvian.mods.kubejs.forge.KubeJSForgeEventHandlerWrapper;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record WrappedForgeEventHandler(
        String className, KubeJSForgeEventHandlerWrapper inner) implements KubeJSForgeEventHandlerWrapper {

    public static Map<String, Class<?>> capturedEvents = new HashMap<>();

    @Override
    public void accept(Event event) {
        capturedEvents.put(className, event.getClass());
        inner.accept(event);
    }

    @NotNull
    @Override
    public Consumer<Event> andThen(@NotNull Consumer<? super Event> after) {
        return inner.andThen(after);
    }
}
