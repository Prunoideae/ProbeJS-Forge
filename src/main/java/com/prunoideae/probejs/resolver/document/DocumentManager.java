package com.prunoideae.probejs.resolver.document;

import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.ProbePaths;
import com.prunoideae.probejs.resolver.document.info.ClassDocument;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DocumentManager {
    public static HashMap<String, List<ClassDocument>> classModifiers = new HashMap<>();
    public static HashMap<String, List<ClassDocument>> classAddition = new HashMap<>();

    public static void addClassModifier(String target, ClassDocument document) {
        classModifiers.computeIfAbsent(target, t -> new ArrayList<>()).add(document);
    }

    public static void addClassAddition(String target, ClassDocument document) {
        classAddition.computeIfAbsent(target, t -> new ArrayList<>()).add(document);
    }

    public static void fromPath(DocumentResolver resolver) throws IOException {
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
        }
    }

    public static void fromFiles(DocumentResolver resolver) throws IOException {
        for (Mod mod : Platform.getMods()) {
            Path filePath = mod.getFilePath();
            if (Files.isRegularFile(filePath) && (filePath.getFileName().toString().endsWith(".jar") || filePath.getFileName().toString().endsWith(".zip"))) {
                ZipFile file = new ZipFile(filePath.toFile());
                ZipEntry entry = file.getEntry("probejs.documents.txt");
                if (entry != null) {
                    ProbeJS.LOGGER.info("Found documents list from %s".formatted(mod.getName()));
                    InputStream stream = file.getInputStream(entry);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(stream), StandardCharsets.UTF_8));
                    List<String> list = reader.lines().collect(Collectors.toList());
                    for (String subEntry : list) {
                        ZipEntry docEntry = file.getEntry(subEntry);
                        if (docEntry != null) {
                            ProbeJS.LOGGER.info("Loading document inside jar - %s".formatted(subEntry));
                            InputStream docStream = file.getInputStream(docEntry);
                            BufferedReader docReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(docStream), StandardCharsets.UTF_8));
                            docReader.lines().forEach(resolver::step);
                        } else {
                            ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(subEntry));
                        }
                    }
                }
            }
        }
    }

    public static void init() {

        try {
            classModifiers.clear();
            classAddition.clear();
            DocumentResolver resolver = new DocumentResolver();
            fromFiles(resolver);
            fromPath(resolver);
            resolver.getDocument().resolveClasses().forEach(document -> {
                if (document.getTarget() == null) {
                    addClassAddition(document.getName(), document);
                } else {
                    addClassModifier(document.getTarget(), document);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
