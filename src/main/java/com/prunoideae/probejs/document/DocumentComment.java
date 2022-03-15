package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.comment.AbstractComment;
import com.prunoideae.probejs.document.comment.CommentHandler;
import com.prunoideae.probejs.formatter.formatter.IFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentComment implements IDecorative, IFormatter {
    private final List<String> documentText;
    private final HashMap<Class<? extends AbstractComment>, List<AbstractComment>> abstractComments = new HashMap<>();

    public DocumentComment(List<String> documentText) {
        this.documentText = documentText.stream().map(String::strip).collect(Collectors.toList());
        this.documentText
                .stream()
                .map(t -> t.startsWith("*") ? t.substring(1).strip() : t)
                .filter(t -> CommentHandler.specialCommentHandler.containsKey(t.split(" ")[0]))
                .map(t -> CommentHandler.specialCommentHandler.get(t.split(" ")[0]).apply(t))
                .forEach(c -> abstractComments.computeIfAbsent(c.getClass(), s -> new ArrayList<>()).add(c));
    }

    public List<String> getRawDocumentText() {
        return documentText;
    }

    public List<AbstractComment> getSpecialCommentsList() {
        return abstractComments.values().stream().flatMap(Collection::stream).toList();
    }

    public List<AbstractComment> getSpecialComments(Class<? extends AbstractComment> clazz) {
        return abstractComments.getOrDefault(clazz, new ArrayList<>());
    }

    public AbstractComment getSpecialComment(Class<? extends AbstractComment> clazz, int index) {
        List<AbstractComment> a = abstractComments.get(clazz);
        return a == null ? null : a.get(index);
    }

    public AbstractComment getSpecialComment(Class<? extends AbstractComment> clazz) {
        return getSpecialComment(clazz, 0);
    }

    public List<String> getDocumentText() {
        return documentText
                .stream()
                .filter(text -> text.startsWith("*")
                        ? !CommentHandler.specialCommentHandler.containsKey(text.substring(1).strip().split(" ")[0])
                        : !CommentHandler.specialCommentHandler.containsKey(text.split(" ")[0]))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return getDocumentText().stream().map(s -> " ".repeat(indent) + s).collect(Collectors.toList());
    }
}
