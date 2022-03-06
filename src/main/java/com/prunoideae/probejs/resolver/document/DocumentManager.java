package com.prunoideae.probejs.resolver.document;

import com.prunoideae.probejs.ProbePaths;
import com.prunoideae.probejs.resolver.document.info.ClassDocument;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class DocumentManager {
    public static HashMap<String, List<ClassDocument>> classModifiers = new HashMap<>();
    public static HashMap<String, List<ClassDocument>> classAddition = new HashMap<>();

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
            File[] files = ProbePaths.DOCS.toFile().listFiles();
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

}
