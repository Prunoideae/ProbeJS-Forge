package com.prunoideae.probejs;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.prunoideae.probejs.compiler.SnippetCompiler;
import com.prunoideae.probejs.compiler.TypingCompiler;
import com.prunoideae.probejs.document.Manager;
import com.prunoideae.probejs.document.comment.CommentHandler;
import com.prunoideae.probejs.document.parser.processor.DocumentProviderHandler;
import com.prunoideae.probejs.old.resolver.document.DocumentManager;
import com.prunoideae.probejs.old.typings.ProbeCompiler;
import com.prunoideae.probejs.old.typings.SpecialFormatters;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.server.ServerSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class ProbeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("probejs")
                        .then(Commands.literal("dump")
                                .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
                                .executes(context -> {
                                    try {
                                        DocumentProviderHandler.init();
                                        CommentHandler.init();
                                        Manager.init();
                                        TypingCompiler.compileGlobal();

                                        //DocumentManager.init();
                                        //export(context.getSource());
                                        //SnippetCompiler.fromKubeDump();
                                        //context.getSource().sendSuccess(new TextComponent("KubeJS registry snippets generated."), false);
                                        //SpecialFormatters.init();
                                        //ProbeCompiler.compileDeclarations();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        context.getSource().sendSuccess(new TextComponent("Uncaught exception happened in wrapper, please report to the Github issue with complete latest.log."), false);
                                    }
                                    context.getSource().sendSuccess(new TextComponent("ProbeJS typing generation finished."), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("clear_cache")
                                .requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
                                .executes(context -> {
                                    Path path = KubeJSPaths.EXPORTED.resolve("cachedEvents.json");
                                    if (Files.exists(path)) {
                                        if (path.toFile().delete()) {
                                            context.getSource().sendSuccess(new TextComponent("Cache files removed."), false);
                                        } else {
                                            context.getSource().sendSuccess(new TextComponent("Failed to remove cache files."), false);
                                        }
                                    } else {
                                        context.getSource().sendSuccess(new TextComponent("No cached files to be cleared."), false);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("recipe_stub")
                                .executes(context -> {
                                    //TODO: add features for exporting stub of recipes
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }

    private static void export(CommandSourceStack source) {
        if (ServerSettings.dataExport != null) {
            return;
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
    }
}
