package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.comment.AbstractComment;
import com.prunoideae.probejs.document.comment.CommentHandler;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentComment implements IDecorative {
    private final List<String> documentText;

    public DocumentComment(List<String> documentText) {
        this.documentText = documentText.stream().map(String::strip).collect(Collectors.toList());
    }

    public List<String> getRawDocumentText() {
        return documentText;
    }

    public List<AbstractComment> getSpecialComments() {
        return documentText
                .stream()
                .map(t -> t.startsWith("*") ? t.substring(1).strip() : t)
                .filter(t -> CommentHandler.specialCommentHandler.containsKey(t.split(" ")[0]))
                .map(t -> CommentHandler.specialCommentHandler.get(t.split(" ")[0]).apply(t))
                .collect(Collectors.toList());
    }

    public List<String> getDocumentText() {
        return documentText
                .stream()
                .filter(text -> text.startsWith("*")
                        ? !CommentHandler.specialCommentHandler.containsKey(text.substring(1).strip().split(" ")[0])
                        : !CommentHandler.specialCommentHandler.containsKey(text.split(" ")[0]))
                .collect(Collectors.toList());
    }
}
