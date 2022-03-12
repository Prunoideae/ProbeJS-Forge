package com.prunoideae.probejs.old.resolver.document.part;

import com.prunoideae.probejs.old.resolver.document.Document;
import com.prunoideae.probejs.old.resolver.document.info.CommentDocument;

import java.util.ArrayList;
import java.util.List;

public class PartTypeDecl {
    private final String name;
    private final String type;
    private CommentDocument comment = null;

    public void setComment(CommentDocument comment) {
        this.comment = comment;
    }

    public PartTypeDecl(String line) {
        line = line.substring(4).strip();
        String[] nameType = line.split("=");
        if (nameType.length != 2)
            throw new RuntimeException("Badly formatted type declaration.");
        name = nameType[0].strip();
        String t = nameType[1].strip();
        if (t.endsWith(";"))
            t = t.substring(0, t.length() - 1).strip();
        type = t;
    }

    public List<String> format(int indent) {
        List<String> lines = new ArrayList<>();
        if (this.comment != null)
            lines.addAll(this.comment.getCommentText(indent));
        lines.add(" ".repeat(indent) + "type %s = %s;\n".formatted(this.name, Document.formatType(this.type)));
        return lines;
    }

    public String getName() {
        return name;
    }
}
