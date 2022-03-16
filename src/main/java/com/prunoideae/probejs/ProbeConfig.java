package com.prunoideae.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ProbeConfig {
    public static ProbeConfig INSTANCE = new ProbeConfig();
    private static final Path CONFIG = KubeJSPaths.CONFIG.resolve("probejs.json");
    public boolean dumpMethod = false;
    public boolean disabled = false;

    private static <E> E fetchPropertyOrDefault(Object key, Map<?, ?> value, E defaultValue) {
        Object v = value.get(key);
        return v == null ? defaultValue : (E) v;
    }

    private ProbeConfig() {
        Path cfg = KubeJSPaths.CONFIG.resolve("probejs.json");
        if (Files.exists(cfg)) {
            try {
                Map<?, ?> obj = new Gson().fromJson(Files.newBufferedReader(cfg), Map.class);
                dumpMethod = fetchPropertyOrDefault("dumpMethod", obj, false);
                disabled = fetchPropertyOrDefault("disabled", obj, false);
            } catch (IOException e) {
                ProbeJS.LOGGER.warn("Cannot read config properties, falling back to defaults.");
            }
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
        } catch (IOException e) {
            ProbeJS.LOGGER.warn("Cannot write config, settings are not saved.");
        }
    }
}
