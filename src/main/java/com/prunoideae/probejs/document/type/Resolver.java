package com.prunoideae.probejs.document.type;

import com.prunoideae.probejs.util.Pair;
import com.prunoideae.probejs.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class Resolver {
    public static IType resolveType(String type) {
        type = type.strip();

        Pair<String, String> splitUnion = StringUtil.splitFirst(type, "<", ">", "|");
        if (splitUnion != null) {
            return new TypeUnion(resolveType(splitUnion.getFirst()), resolveType(splitUnion.getSecond()));
        }

        Pair<String, String> splitIntersection = StringUtil.splitFirst(type, "<", ">", "&");
        if (splitIntersection != null) {
            return new TypeIntersection(resolveType(splitIntersection.getFirst()), resolveType(splitIntersection.getSecond()));
        }

        if (type.endsWith("[]")) {
            return new TypeArray(resolveType(type.substring(0, type.length() - 2)));
        }

        if (type.endsWith(">")) {
            int indexLeft = type.indexOf("<");
            String rawType = type.substring(0, indexLeft);
            String typeParams = type.substring(indexLeft + 1, type.length() - 1);
            List<String> params = StringUtil.splitLayer(typeParams, "<", ">", ",");
            return new TypeParameterized(resolveType(rawType), params.stream().map(Resolver::resolveType).collect(Collectors.toList()));
        }
        return new TypeNamed(type);
    }
}
