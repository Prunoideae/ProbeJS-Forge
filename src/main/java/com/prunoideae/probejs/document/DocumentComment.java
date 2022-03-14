package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.comment.AbstractComment;
import com.prunoideae.probejs.document.comment.CommentHandler;
import com.prunoideae.probejs.formatter.formatter.IFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentComment implements IDecorative, IFormatter {
    private final List<String> documentText;
    private final HashMap<Class<? extends AbstractComment>, AbstractComment> abstractComments = new HashMap<>();

    public DocumentComment(List<String> documentText) {
        this.documentText = documentText.stream().map(String::strip).collect(Collectors.toList());
        documentText
                .stream()
                .map(t -> t.startsWith("*") ? t.substring(1).strip() : t)
                .filter(t -> CommentHandler.specialCommentHandler.containsKey(t.split(" ")[0]))
                .map(t -> CommentHandler.specialCommentHandler.get(t.split(" ")[0]).apply(t))
                .forEach(c -> abstractComments.put(c.getClass(), c));
    }

    public List<String> getRawDocumentText() {
        return documentText;
    }

    public List<AbstractComment> getSpecialComments() {
        return abstractComments.values().stream().toList();
    }

    public AbstractComment getSpecialComment(Class<? extends AbstractComment> clazz) {
        return abstractComments.get(clazz);
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
