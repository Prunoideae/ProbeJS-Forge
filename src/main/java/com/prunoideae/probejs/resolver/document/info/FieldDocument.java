package com.prunoideae.probejs.resolver.document.info;

import com.prunoideae.probejs.resolver.document.Document;

import java.util.ArrayList;
import java.util.List;

public class FieldDocument {
    private CommentDocument commentDocument;
    private boolean isStatic = false;
    private boolean isFinal = false;
    private final String name;
    private final String type;

    public FieldDocument(String fieldComponent) {
        boolean atHead = false;
        while (!atHead) {
            fieldComponent = Document.removeBlank(fieldComponent);
            if (fieldComponent.startsWith("static")) {
                isStatic = true;
                fieldComponent = fieldComponent.substring(6);
            } else if (fieldComponent.startsWith("readonly")) {
                isFinal = true;
                fieldComponent = fieldComponent.substring(8);
            } else {
                atHead = true;
            }
        }

        name = fieldComponent.substring(0, fieldComponent.indexOf(":"));
        fieldComponent = Document.removeBlank(fieldComponent);
        fieldComponent = fieldComponent.substring(name.length() + 1);
        if (fieldComponent.endsWith(";"))
            fieldComponent = fieldComponent.substring(0, fieldComponent.length() - 1);
        type = Document.removeBlank(fieldComponent);
    }

    @Override
    public String toString() {
        return "FieldDocument{" +
                "commentDocument=" + commentDocument +
                ", isStatic=" + isStatic +
                ", isFinal=" + isFinal +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public void setComment(CommentDocument commentDocument) {
        this.commentDocument = commentDocument;
    }

    public String getName() {
        return name;
    }

    public List<String> format(int indent) {
        List<String> lines = new ArrayList<>();
        if (commentDocument != null)
            lines.addAll(commentDocument.getCommentText(indent));
        String field = "%s: %s".formatted(this.name, Document.formatType(this.type));
        if (this.isFinal)
            field = "readonly " + field;
        if (this.isStatic)
            field = "static " + field;
        field = " ".repeat(indent) + field;
        lines.add(field);
        return lines;
    }

}
