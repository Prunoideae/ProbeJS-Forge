package com.prunoideae.probejs.typings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.KubeJSPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubeCompiler {
    public static class KubeDump {
        public Map<String, Map<String, List<String>>> tags;
        public Map<String, List<String>> registries;

        public KubeDump(Map<String, Map<String, List<String>>> tags, Map<String, List<String>> registries) {
            this.tags = tags;
            this.registries = registries;
        }

        @Override
        public String toString() {
            return "KubeDump{" +
                    "tags=" + tags +
                    ", registries=" + registries +
                    '}';
        }

        public JsonObject toSnippet() {
            JsonObject resultJson = new JsonObject();
            // Compile normal entries to snippet
            for (Map.Entry<String, List<String>> entry : this.registries.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue();
                Map<String, List<String>> byModMembers = new HashMap<>();
                members.stream().map(rl -> rl.split(":")).forEach(rl -> {
                    if (!byModMembers.containsKey(rl[0]))
                        byModMembers.put(rl[0], new ArrayList<>());
                    byModMembers.get(rl[0]).add(rl[1]);
                });
                byModMembers.forEach((mod, mems) -> {
                    JsonObject modMembers = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    prefixes.add("@%s.%s".formatted(mod, type));
                    modMembers.add("prefix", prefixes);
                    modMembers.addProperty("body", "\"%s:${1|%s|}\"".formatted(mod, String.join(",", mems)));
                    resultJson.add("%s_%s".formatted(type, mod), modMembers);
                });
            }

            // Compile tag entries to snippet
            for (Map.Entry<String, Map<String, List<String>>> entry : this.tags.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue().keySet().stream().toList();
                Map<String, List<String>> byModMembers = new HashMap<>();
                members.stream().map(rl -> rl.split(":")).forEach(rl -> {
                    if (!byModMembers.containsKey(rl[0]))
                        byModMembers.put(rl[0], new ArrayList<>());
                    byModMembers.get(rl[0]).add(rl[1]);
                });
                byModMembers.forEach((mod, mems) -> {
                    JsonObject modMembers = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    prefixes.add("@%s.tags.%s".formatted(mod, type));
                    modMembers.add("prefix", prefixes);
                    modMembers.addProperty("body", "\"#%s:${1|%s|}\"".formatted(mod, String.join(",", mems)));
                    resultJson.add("%s_tag_%s".formatted(type, mod), modMembers);
                });
            }

            return resultJson;
        }

        public void writeDumpTags(Path path) throws IOException {
            BufferedWriter writer = Files.newBufferedWriter(path);
            List<String> types = new ArrayList<>();
            this.tags.forEach((type, members) -> {
                Map<String, List<String>> byMods = new HashMap<>();
                members.forEach((resourceLocation, tagMembers) -> {
                    String[] rl = resourceLocation.split(":");
                    rl[1] = rl[1].replace("/", "_");
                    byMods.computeIfAbsent(rl[0], key -> new ArrayList<>()).add("%s:%s".formatted(rl[1], new Gson().toJson(tagMembers)));
                });
                List<String> modsString = byMods.entrySet().stream().map(entry -> "%s:{%s}".formatted(entry.getKey(), String.join(",\n", entry.getValue()))).collect(Collectors.toList());
                types.add("%s:{%s}".formatted(type, String.join(",\n", modsString)));
            });
            writer.write("// priority: 1000\n");
            writer.write("const tags = {%s}".formatted(String.join(",\n", types)));
            writer.flush();
        }
    }

    public static void fromKubeDump() throws IOException {
        Path kubePath = KubeJSPaths.EXPORTED.resolve("kubejs-server-export.json");
        if (kubePath.toFile().canRead()) {
            Path codePath = KubeJSPaths.DIRECTORY.resolve(".vscode");
            if (Files.notExists(codePath)) {
                Files.createDirectories(codePath);
            }
            Path codeFile = codePath.resolve("probe.code-snippets");

            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(kubePath);
            KubeDump kubeDump = gson.fromJson(reader, KubeDump.class);
            BufferedWriter writer = Files.newBufferedWriter(codeFile);
            writer.write(gson.toJson(kubeDump.toSnippet()));
            writer.flush();

            kubeDump.writeDumpTags(KubeJSPaths.SERVER_SCRIPTS.resolve("dumps.js"));
            kubeDump.writeDumpTags(KubeJSPaths.STARTUP_SCRIPTS.resolve("dumps.js"));
            kubeDump.writeDumpTags(KubeJSPaths.CLIENT_SCRIPTS.resolve("dumps.js"));
        }
    }
}
