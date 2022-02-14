package com.prunoideae.probejs.plugin;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.IEventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.ListJS;

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
}
