package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.document.DocumentMethod;
import com.prunoideae.probejs.info.MethodInfo;

import java.util.List;

public class FormatterMethod extends DocumentedFormatter<DocumentMethod> implements IFormatter {
    private final MethodInfo methodInfo;

    public FormatterMethod(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        return null;
    }

    public String getBean() {
        return null;
    }

    public List<String> formatBean() {
        return null;
    }
}
