package com.prunoideae.probejs.resolver.document;

import com.prunoideae.probejs.resolver.Util;
import com.prunoideae.probejs.resolver.document.info.ClassDocument;
import com.prunoideae.probejs.resolver.document.info.CommentDocument;
import com.prunoideae.probejs.resolver.document.part.ComponentComment;
import com.prunoideae.probejs.resolver.document.part.PartClass;
import com.prunoideae.probejs.resolver.document.part.PartModRequirement;
import com.prunoideae.probejs.resolver.document.part.PartTarget;
import com.prunoideae.probejs.resolver.handler.IStateHandler;
import com.prunoideae.probejs.typings.TSGlobalClassFormatter;
import dev.architectury.platform.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the entire .d.ts declaration file.
 */
public class Document implements IStateHandler<String> {
    private final List<Object> documentParts = new ArrayList<>();

    public static String removeBlank(String s) {
        while (s.startsWith(" ") || s.startsWith("\t"))
            s = s.substring(1);
        return s;
    }

    public static int getBlank(String s) {
        int length = 0;
        if (s.charAt(length) == ' ')
            length++;
        return length;
    }

    public static String formatType(String s) {
        int arrayDepth = 0;
        while (s.endsWith("[]")) {
            s = s.substring(0, s.length() - 2);
            arrayDepth++;
        }

        if (s.startsWith("\"") || s.startsWith("'"))
            return s;

        List<String> paramString = Util.splitString(s, ",", "<", ">");
        return paramString.stream().map(p -> {
            p = removeBlank(p);
            List<String> testParam = Util.unwrapString(p, "<", ">");
            String baseTypeName = testParam.get(0);
            baseTypeName = TSGlobalClassFormatter.resolvedClassName.getOrDefault(baseTypeName, baseTypeName);
            if (testParam.size() == 1) {
                return baseTypeName;
            } else {
                return "%s<%s>".formatted(baseTypeName, formatType(testParam.get(1)));
            }
        }).collect(Collectors.joining(", ")) + "[]".repeat(arrayDepth);
    }

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        // If null is passed in, then it means EOF is reached.
        if (element == null) {
            stack.remove(this);
            return;
        }

        if (element.contains("/**")) {
            ComponentComment comment = new ComponentComment(element);
            this.documentParts.add(comment);
            stack.add(comment);
            return;
        }

        element = removeBlank(element);

        if (element.startsWith("@Target")) {
            this.documentParts.add(new PartTarget(element));
        } else if (element.startsWith("@Mod")) {
            this.documentParts.add(new PartModRequirement(element));
        } else if (element.startsWith("class") || element.startsWith("interface")) {
            PartClass clazz = new PartClass(element);
            this.documentParts.add(clazz);
            stack.add(clazz);
        }
    }

    public List<ClassDocument> resolveClasses() {
        List<ClassDocument> classDocuments = new ArrayList<>();
        List<PartModRequirement> mods = new ArrayList<>();
        PartTarget target = null;
        ComponentComment comment = null;
        for (Object o : this.documentParts) {
            if (o instanceof PartTarget)
                target = (PartTarget) o;
            else if (o instanceof PartModRequirement)
                mods.add((PartModRequirement) o);
            else if (o instanceof ComponentComment) {
                comment = (ComponentComment) o;
            } else if (o instanceof PartClass) {
                ClassDocument document = new ClassDocument((PartClass) o);
                if (target != null)
                    document.setTarget(target.targetName);
                if (comment != null)
                    document.setComment(new CommentDocument(comment));
                if (mods.stream().allMatch(part -> Platform.isModLoaded(part.targetName)))
                    classDocuments.add(document);
                comment = null;
                target = null;
                mods.clear();
            }
        }
        return classDocuments;
    }
}
