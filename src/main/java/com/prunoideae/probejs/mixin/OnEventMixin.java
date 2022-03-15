package com.prunoideae.probejs.mixin;

import com.prunoideae.probejs.plugin.WrappedEventHandler;
import dev.latvian.mods.kubejs.event.EventsJS;
import dev.latvian.mods.kubejs.event.IEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EventsJS.class)
public class OnEventMixin {

    @ModifyVariable(method = "listen", argsOnly = true, at = @At("HEAD"), remap = false)
    private IEventHandler listen(IEventHandler handler, String id) {
        if (!(handler instanceof WrappedEventHandler))
            return new WrappedEventHandler(id, handler);
        return handler;
    }
}
