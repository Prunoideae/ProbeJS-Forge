package com.prunoideae.probejs.document;

import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.ProbePaths;
import com.prunoideae.probejs.document.parser.processor.Document;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Manager {
    public static List<DocumentClass> classDocuments = new ArrayList<>();

    public static void fromPath(Document document) throws IOException {
        File[] files = ProbePaths.DOCS.toFile().listFiles();
        List<File> filesSorted = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.stream(files).toList());
        filesSorted.sort(Comparator.comparing(File::getName));
        for (File f : filesSorted) {
            if (!f.getName().endsWith(".d.ts"))
                return;
            if (f.isDirectory())
                return;
            BufferedReader reader = Files.newBufferedReader(f.toPath());
            reader.lines().forEach(document::step);
        }
    }

    public static void fromFiles(Document document) throws IOException {
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
                            docReader.lines().forEach(document::step);
                        } else {
                            ProbeJS.LOGGER.warn("Document from file is not found - %s".formatted(subEntry));
                        }
                    }
                }
            }
        }
    }

    public static void init() {
        Document documentState = new Document();
        try {
            fromFiles(documentState);
            fromPath(documentState);
        } catch (IOException e) {
            e.printStackTrace();
        }

        classDocuments.clear();
        classDocuments.addAll(documentState
                .getDocument()
                .getDocuments()
                .stream()
                .filter(d -> d instanceof DocumentClass)
                .map(d -> (DocumentClass) d)
                .collect(Collectors.toList()));
        ProbeJS.LOGGER.info(classDocuments);
    }
}
