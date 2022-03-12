package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.document.FieldDocument;
import com.prunoideae.probejs.document.IDocumented;
import com.prunoideae.probejs.info.FieldInfo;

import java.util.List;

public class FieldFormatter implements IDocumented<FieldDocument>, IFormatter {
    private final FieldInfo fieldInfo;

    public FieldFormatter(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public void setDocument(FieldDocument document) {

    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return null;
    }
}
