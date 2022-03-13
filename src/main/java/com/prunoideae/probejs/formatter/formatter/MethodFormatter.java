package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.document.IDocumented;
import com.prunoideae.probejs.document.DocumentMethod;

import java.util.List;

public class MethodFormatter implements IDocumented<DocumentMethod>, IFormatter {
    @Override
    public void setDocument(DocumentMethod document) {

    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return null;
    }
}
