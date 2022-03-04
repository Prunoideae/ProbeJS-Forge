package com.prunoideae.probejs.plugin;

import dev.latvian.kubejs.KubeJSPlugin;
import dev.latvian.kubejs.event.IEventHandler;
import dev.latvian.kubejs.script.BindingsEvent;
import dev.latvian.kubejs.util.ListJS;

public class ProbePlugin extends KubeJSPlugin {
    @Override
    public void addBindings(BindingsEvent event) {
        event.addFunction("captureEvent", args -> onWrappedEvent(event, args), null, IEventHandler.class);
    }

    private static Object onWrappedEvent(BindingsEvent event, Object[] args) {
        for (Object o : ListJS.orSelf(args[0])) {
            String e = String.valueOf(o);
            event.type.manager.get().events.listen(e, new WrappedEventHandler(e, (IEventHandler) args[1]));
        }
        return null;
    }

    private static Object onWrappedForgeEvent(BindingsEvent event, Object[] args) {
        return null;
    }
}
