package com.prunoideae.probejs.resolver.document.info;

import com.prunoideae.probejs.resolver.Util;
import com.prunoideae.probejs.resolver.document.Document;
import com.prunoideae.probejs.toucher.ClassInfo;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MethodDocument {
    private CommentDocument commentDocument;
    private boolean isStatic = false;
    private final String name;
    private final String returnType;
    private final List<String> params;

    public MethodDocument(String methodComponent) {
        boolean atHead = false;
        while (!atHead) {
            methodComponent = Document.removeBlank(methodComponent);
            if (methodComponent.startsWith("static")) {
                isStatic = true;
                methodComponent = methodComponent.substring(6);
            } else if (methodComponent.startsWith("readonly")) {
                methodComponent = methodComponent.substring(8);
            } else {
                atHead = true;
            }
        }
        if (methodComponent.endsWith(";"))
            methodComponent = methodComponent.substring(0, methodComponent.length() - 1);
        List<String> nameParamReturn = Util.unwrapString(methodComponent, "(", ")");
        if (nameParamReturn.size() != 3) {
            throw new RuntimeException("Bad method format. %s".formatted(nameParamReturn));
        }
        this.name = nameParamReturn.get(0);
        this.params = Util.splitString(nameParamReturn.get(1), ",", "<", ">");
        this.returnType = Document.removeBlank(nameParamReturn.get(2).substring(1));
    }

    @Override
    public String toString() {
        return "MethodDocument{" +
                "commentDocument=" + commentDocument +
                ", isStatic=" + isStatic +
                ", name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", params=" + params +
                '}';
    }

    public void setComment(CommentDocument commentDocument) {
        this.commentDocument = commentDocument;
    }

    public String getName() {
        return name;
    }

    public boolean compareType(String str, Type type) {
        if (str == null || type == null)
            return false;

        while (type instanceof GenericArrayType) {
            type = ((GenericArrayType) type).getGenericComponentType();
            if (str.endsWith("[]"))
                str = str.substring(0, str.length() - 2);
            else
                return false;
        }

        if (type instanceof Class<?>)
            return ((Class<?>) type).getName().equals(str);
        if (type instanceof WildcardType)
            return str.equals("any");
        if (type instanceof TypeVariable<?>)
            return ((TypeVariable<?>) type).getName().equals(str);

        if (type instanceof ParameterizedType) {
            List<String> unwrapped = Util.unwrapString(str, "<", ">");
            if (unwrapped.size() != 3)
                return false;
            if (!compareType(unwrapped.get(0), ((ParameterizedType) type).getRawType()))
                return false;
            List<String> parameterized = Arrays.stream(unwrapped.get(1).split(",")).map(String::strip).collect(Collectors.toList());
            if (parameterized.size() != ((ParameterizedType) type).getActualTypeArguments().length)
                return false;
            Type[] actual = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < parameterized.size(); i++)
                if (!compareType(parameterized.get(i), actual[i]))
                    return false;
        }
        return true;
    }

    public boolean isMatch(ClassInfo.MethodInfo method) {
        if (method.isStatic() != this.isStatic)
            return false;

        HashMap<String, String> paramTypes = new HashMap<>();
        HashMap<String, Type> methodTypes = new HashMap<>();
        params.forEach(s -> {
            String[] ss = s.split(":");
            paramTypes.put(ss[0].strip(), ss[1].strip());
        });
        method.getParamsInfo().forEach(paramInfo -> methodTypes.put(paramInfo.getName(), paramInfo.getType()));
        if (!methodTypes.keySet().equals(paramTypes.keySet()))
            return false;
        List<String> paramKeys = paramTypes.keySet().stream().toList();
        for (String paramKey : paramKeys) {
            Type methodParam = methodTypes.remove(paramKey);
            String docParam = paramTypes.get(paramKey);
            if (!compareType(docParam, methodParam))
                return false;
        }
        return true;
    }

    public String getReturnType() {
        return returnType;
    }

    public CommentDocument getCommentDocument() {
        return commentDocument;
    }

    public HashMap<String, String> getParams() {
        HashMap<String, String> paramsMap = new HashMap<>();
        for (String param : params.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList())) {
            String[] kv = param.split(":");
            paramsMap.put(kv[0].strip(), kv[1].strip());
        }
        return paramsMap;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
