package com.prunoideae.probejs.mixin;

import com.prunoideae.probejs.plugin.WrappedEventHandler;
import dev.latvian.mods.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.mods.kubejs.event.IEventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.ListJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BuiltinKubeJSPlugin.class)
public class OnEventMixin {

    @Overwrite(remap = false)
    private static Object onEvent(BindingsEvent event, Object[] args) {
        for (Object o : ListJS.orSelf(args[0])) {
            String e = String.valueOf(o);
            event.type.manager.get().events.listen(e, new WrappedEventHandler(e, (IEventHandler) args[1]));

        }
        return null;
    }
}
