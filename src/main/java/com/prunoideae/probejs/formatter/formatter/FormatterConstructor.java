package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.ConstructorInfo;
import com.prunoideae.probejs.info.MethodInfo;
import com.prunoideae.probejs.info.TypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatterConstructor implements IFormatter {
    private final ConstructorInfo constructor;

    public FormatterConstructor(ConstructorInfo constructor) {
        this.constructor = constructor;
    }

    private String formatTypeParameterized(TypeInfo info) {
        StringBuilder sb = new StringBuilder(new FormatterType(info).format(0, 0));
        if (info.isClazz() && info.getRawType() instanceof Class<?> clazz) {
            if (clazz.getTypeParameters().length != 0)
                sb.append("<%s>".formatted(String.join(", ", Collections.nCopies(clazz.getTypeParameters().length, "any"))));
        }
        return sb.toString();
    }

    private String formatParams() {
        List<MethodInfo.ParamInfo> params = constructor.getParams();
        List<String> paramStrings = new ArrayList<>();
        for (MethodInfo.ParamInfo param : params) {
            paramStrings.add("%s: %s".formatted(NameResolver.getNameSafe(param.getName()), formatTypeParameterized(param.getType())));
        }
        return String.join(", ", paramStrings);
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        formatted.add(" ".repeat(indent) + "constructor(%s);".formatted(formatParams()));
        return formatted;
    }
}
