package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.DocumentField;
import com.prunoideae.probejs.document.comment.special.CommentHidden;
import com.prunoideae.probejs.info.FieldInfo;

import java.util.ArrayList;
import java.util.List;

public class FormatterField extends DocumentedFormatter<DocumentField> implements IFormatter {
    private final FieldInfo fieldInfo;

    public FormatterField(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document.getComment();
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null)
                return formatted;
            formatted.addAll(comment.format(indent, stepIndent));
        }
        List<String> elements = new ArrayList<>();
        if (fieldInfo.isStatic())
            elements.add("static");
        if (fieldInfo.isFinal())
            elements.add("readonly");
        elements.add(fieldInfo.getName());
        elements.add(":");
        elements.add(new FormatterType(fieldInfo.getType()).format(0, 0));
        formatted.add(String.join(" ", elements) + ";");
        return formatted;
    }
}
