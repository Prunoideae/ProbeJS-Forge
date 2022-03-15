package com.prunoideae.probejs.compiler;

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

public class SnippetCompiler {
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
                byModMembers.forEach((mod, modMembers) -> {
                    JsonObject modMembersJson = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    prefixes.add("@%s.%s".formatted(mod, type));
                    modMembersJson.add("prefix", prefixes);
                    modMembersJson.addProperty("body", "\"%s:${1|%s|}\"".formatted(mod, String.join(",", modMembers)));
                    resultJson.add("%s_%s".formatted(type, mod), modMembersJson);
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
                byModMembers.forEach((mod, modMembers) -> {
                    JsonObject modMembersJson = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    prefixes.add("@%s.tags.%s".formatted(mod, type));
                    modMembersJson.add("prefix", prefixes);
                    modMembersJson.addProperty("body", "\"#%s:${1|%s|}\"".formatted(mod, String.join(",", modMembers)));
                    resultJson.add("%s_tag_%s".formatted(type, mod), modMembersJson);
                });
            }

            return resultJson;
        }

    }

    public static void compile() throws IOException {
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

        }
    }
}
