package com.prunoideae.probejs;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Prunoideae
 */
@Mod("probejs")
public class ProbeJS {
    public ProbeJS() {
        CommandRegistrationEvent.EVENT.register(((dispatcher, selection) -> ProbeCommands.register(dispatcher)));
    }
}