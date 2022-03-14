package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.comment.CommentUtil;
import com.prunoideae.probejs.formatter.formatter.IFormatter;

import java.util.ArrayList;
import java.util.List;

public class DocumentClass implements IConcrete, IFormatter {
    private DocumentComment comment;
    private final List<DocumentField> fields = new ArrayList<>();
    private final List<DocumentMethod> methods = new ArrayList<>();

    public DocumentComment getComment() {
        return comment;
    }

    public void acceptProperty(IDocument document) {
        if (document instanceof DocumentProperty) {
            DocumentComment comment = ((DocumentProperty) document).getComment();
            if (!CommentUtil.isLoaded(comment))
                return;
        }

        if (document instanceof DocumentField) {
            fields.add((DocumentField) document);
        }
        if (document instanceof DocumentMethod) {
            methods.add((DocumentMethod) document);
        }
    }

    public List<DocumentField> getFields() {
        return fields;
    }

    public List<DocumentMethod> getMethods() {
        return methods;
    }

    @Override
    public void acceptDeco(List<IDecorative> decorates) {
        for (IDecorative decorative : decorates) {
            if (decorative instanceof DocumentComment) {
                this.comment = (DocumentComment) decorative;
            }
        }
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return null;
    }
}
