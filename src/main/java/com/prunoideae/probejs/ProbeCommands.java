package com.prunoideae.probejs;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.prunoideae.probejs.typings.KubeCompiler;
import com.prunoideae.probejs.typings.ProbeCompiler;
import com.prunoideae.probejs.typings.SpecialFormatters;
import dev.latvian.mods.kubejs.server.ServerSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;

import java.util.Collection;

public class ProbeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
                                .executes(context -> {
                                    try {
                                        export(context.getSource());
                                        KubeCompiler.fromKubeDump();
                                        SpecialFormatters.init();
                                        ProbeCompiler.compileDeclarations();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    context.getSource().sendSuccess(new TextComponent("Typing generation finished."), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

    private static int export(CommandSourceStack source) {
        if (ServerSettings.dataExport != null) {
            return 0;
        }

        ServerSettings.source = source;
        ServerSettings.dataExport = new JsonObject();
        source.sendSuccess(new TextComponent("Reloading server and exporting data..."), false);

        MinecraftServer minecraftServer = source.getServer();
        PackRepository packRepository = minecraftServer.getPackRepository();
        WorldData worldData = minecraftServer.getWorldData();
        Collection<String> collection = packRepository.getSelectedIds();
        packRepository.reload();
        Collection<String> collection2 = Lists.newArrayList(collection);
        Collection<String> collection3 = worldData.getDataPackConfig().getDisabled();

        for (String string : packRepository.getAvailableIds()) {
            if (!collection3.contains(string) && !collection2.contains(string)) {
                collection2.add(string);
            }
        }

        ReloadCommand.reloadPacks(collection2, source);
        return 1;
    }
}
