package com.prunoideae.probejs.resolver.document;

import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.resolver.document.info.ClassDocument;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.*;

public class DocumentManager {
    public static HashMap<String, List<ClassDocument>> classModifiers = new HashMap<>();
    public static HashMap<String, List<ClassDocument>> classAddition = new HashMap<>();
    public static final Path DOCUMENT = KubeJSPaths.DIRECTORY.resolve("docs");

    public static void addClassModifier(String target, ClassDocument document) {
        classModifiers.computeIfAbsent(target, t -> new ArrayList<>()).add(document);
    }

    public static void addClassAddition(String target, ClassDocument document) {
        classAddition.computeIfAbsent(target, t -> new ArrayList<>()).add(document);
    }

    public static void init() {

        try {
            classModifiers.clear();
            classAddition.clear();
            DocumentResolver resolver = new DocumentResolver();
            File[] files = DOCUMENT.toFile().listFiles();
            List<File> filesSorted = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.stream(files).toList());
            filesSorted.sort(Comparator.comparing(File::getName));
            for (File f : filesSorted) {
                if (!f.getName().endsWith(".d.ts"))
                    return;
                if (f.isDirectory())
                    return;
                BufferedReader reader = Files.newBufferedReader(f.toPath());
                reader.lines().forEach(resolver::step);
                resolver.getDocument().resolveClasses().forEach(document -> {
                    if (document.getTarget() == null) {
                        addClassAddition(document.getName(), document);
                    } else {
                        addClassModifier(document.getTarget(), document);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        if (Files.notExists(DOCUMENT, new LinkOption[0])) {
            UtilsJS.tryIO(() -> {
                Files.createDirectories(DOCUMENT);
            });
        }
    }
}
