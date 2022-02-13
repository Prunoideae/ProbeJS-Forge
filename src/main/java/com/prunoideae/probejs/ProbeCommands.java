package com.prunoideae.probejs;

import com.mojang.brigadier.CommandDispatcher;
import com.prunoideae.probejs.dump.ProbeDump;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ProbeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .then(Commands.literal("startup")
                                        .executes(context -> ProbeDump.dump(ScriptType.STARTUP)))
                                .then(Commands.literal("server")
                                        .executes(context -> ProbeDump.dump(ScriptType.SERVER)))
                                .then(Commands.literal("client")
                                        .executes(context -> ProbeDump.dump(ScriptType.CLIENT)))));
    }
}
