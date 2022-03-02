package com.prunoideae.probejs;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Prunoideae
 */
@Mod("probejs")
public class ProbeJS {
    public static final Logger LOGGER = LogManager.getLogger("probejs");

    public ProbeJS() {
        CommandRegistrationEvent.EVENT.register(((dispatcher, selection) -> ProbeCommands.register(dispatcher)));
    }

}