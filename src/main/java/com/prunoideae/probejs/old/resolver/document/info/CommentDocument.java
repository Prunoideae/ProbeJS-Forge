package com.prunoideae.probejs.old.resolver.document.info;

import com.prunoideae.probejs.old.resolver.document.Document;
import com.prunoideae.probejs.old.resolver.document.part.ComponentComment;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommentDocument {
    private final List<String> rawCommentText;

    public CommentDocument(ComponentComment comment) {
        this.rawCommentText = comment.getComments();
    }

    @Override
    public String toString() {
        return "CommentDocument{" +
                "rawCommentText=" + rawCommentText +
                '}';
    }

    public List<String> getCommentText() {
        return getCommentText(0);
    }

    public List<String> getCommentText(int indentation) {
        return rawCommentText.stream().filter(s -> {
            String d = s.strip();
            if (d.startsWith("*"))
                d = d.substring(1).strip();
            if (d.startsWith("@param")) {
                String[] parts = d.split(" ");
                if (parts.length < 3)
                    return true;
                String type = parts[1];
                return !(type.contains("{") && type.contains("}"));
            }
            return !d.startsWith("@returns");
        }).map(s -> " ".repeat(indentation) + s).collect(Collectors.toList());
    }

    public HashMap<String, String> getParamModifiers() {
        HashMap<String, String> params = new HashMap<>();
        rawCommentText.stream()
                .map(Document::removeBlank)
                .filter(s -> s.startsWith("*"))
                .map(s -> s.substring(1))
                .map(Document::removeBlank)
                .filter(s -> s.startsWith("@param "))
                .map(s -> s.substring(7))
                .filter(s -> s.contains("{") && s.contains("}"))
                .forEach(s -> {
                    String[] typeParam = s.split(" ");
                    if (typeParam.length < 2)
                        return;
                    if (!typeParam[0].contains("{") || !typeParam[0].contains("}"))
                        return;
                    params.put(typeParam[1].strip(), typeParam[0].substring(1, typeParam[0].length() - 1));
                });
        return params;
    }

    public String getReturnTypeModifier() {
        Optional<String> first = rawCommentText.stream()
                .map(Document::removeBlank)
                .filter(s -> s.startsWith("*"))
                .map(Document::removeBlank)
                .filter(s -> s.startsWith("@returns "))
                .map(s -> s.substring(9))
                .findFirst();
        return first.orElse(null);
    }

    public List<String> getRawCommentText() {
        return rawCommentText;
    }
}
