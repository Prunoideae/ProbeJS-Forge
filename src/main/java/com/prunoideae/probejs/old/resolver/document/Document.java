package com.prunoideae.probejs.old.resolver.document;

import com.prunoideae.probejs.document.parser.processor.StringUtil;
import com.prunoideae.probejs.old.resolver.document.info.ClassDocument;
import com.prunoideae.probejs.old.resolver.document.info.ClassDocuments;
import com.prunoideae.probejs.old.resolver.document.info.CommentDocument;
import com.prunoideae.probejs.old.resolver.document.part.*;
import com.prunoideae.probejs.document.parser.handler.IStateHandler;
import com.prunoideae.probejs.old.typings.TSGlobalClassFormatter;
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

    public static String formatType(String s) {


        if (s.startsWith("\"") || s.startsWith("'"))
            return s;

        List<String> unionString = StringUtil.splitString(s, "|", "<", ">");
        if (unionString.size() > 1) {
            return unionString.stream().map(String::strip).map(Document::formatType).collect(Collectors.joining("|"));
        }

        List<String> intersectionString = StringUtil.splitString(s, "&", "<", ">");
        if (intersectionString.size() > 1) {
            return intersectionString.stream().map(String::strip).map(Document::formatType).collect(Collectors.joining("&"));
        }

        List<String> paramString = StringUtil.splitString(s, ",", "<", ">");
        if (paramString.size() > 1) {
            return paramString.stream().map(String::strip).map(Document::formatType).collect(Collectors.joining(","));
        }

        int arrayDepth = 0;
        while (s.endsWith("[]")) {
            s = s.substring(0, s.length() - 2);
            arrayDepth++;
        }

        List<String> param = StringUtil.unwrapString(s, "<", ">");
        String baseType = param.get(0);
        baseType = TSGlobalClassFormatter.resolvedClassName.getOrDefault(baseType, baseType);
        if (param.size() == 1) {
            return baseType + "[]".repeat(arrayDepth);
        } else {
            return "%s<%s>".formatted(baseType, formatType(param.get(1))) + "[]".repeat(arrayDepth);
        }
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

        element = element.strip();

        if (element.startsWith("@Target")) {
            this.documentParts.add(new PartTarget(element));
        } else if (element.startsWith("@Mod")) {
            this.documentParts.add(new PartModRequirement(element));
        } else if (element.startsWith("type")) {
            this.documentParts.add(new PartTypeDecl(element));
        } else if (element.startsWith("class") || element.startsWith("interface")) {
            PartClass clazz = new PartClass(element);
            this.documentParts.add(clazz);
            stack.add(clazz);
        }
    }

    public ClassDocuments resolveClasses() {
        List<ClassDocument> classDocuments = new ArrayList<>();
        List<PartTypeDecl> typeDeclDocuments = new ArrayList<>();
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
            } else if (o instanceof PartTypeDecl typeDecl) {
                if (comment != null)
                    typeDecl.setComment(new CommentDocument(comment));
                if (mods.stream().allMatch(part -> Platform.isModLoaded(part.targetName)))
                    typeDeclDocuments.add(typeDecl);
                comment = null;
                target = null;
                mods.clear();
            }
        }
        return new ClassDocuments(classDocuments, typeDeclDocuments);
    }
}
