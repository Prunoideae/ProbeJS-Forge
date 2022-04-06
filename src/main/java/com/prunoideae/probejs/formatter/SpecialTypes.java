package com.prunoideae.probejs.formatter;

import com.prunoideae.probejs.formatter.formatter.FormatterType;
import com.prunoideae.probejs.formatter.formatter.IFormatter;
import com.prunoideae.probejs.info.MethodInfo;
import com.prunoideae.probejs.info.type.ITypeInfo;
import com.prunoideae.probejs.info.type.TypeInfoClass;
import com.prunoideae.probejs.info.type.TypeInfoParameterized;
import com.prunoideae.probejs.info.type.TypeInfoVariable;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.util.BuilderBase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;

public class SpecialTypes {

    public static Set<Class<?>> skippedSpecials = new HashSet<>();

    private static class FormatterLambda {
        private final MethodInfo info;

        private FormatterLambda(MethodInfo info) {
            this.info = info;
        }

        public String format(ITypeInfo typeInfo) {
            List<ITypeInfo> paramTypes = new ArrayList<>();
            if (typeInfo instanceof TypeInfoParameterized parameterized) {
                paramTypes.addAll(parameterized.getParamTypes());
            }

            List<String> formattedParam = new ArrayList<>();
            for (MethodInfo.ParamInfo param : info.getParams()) {
                ITypeInfo resolvedType = param.getType();
                if (resolvedType instanceof TypeInfoVariable) {
                    resolvedType = paramTypes.isEmpty() ? new TypeInfoClass(Object.class) : paramTypes.remove(0);
                }
                formattedParam.add("%s: %s".formatted(param.getName(), new FormatterType(resolvedType).format(0, 0)));
            }
            ITypeInfo resolvedReturn = info.getReturnType();
            if (resolvedReturn instanceof TypeInfoVariable) {
                resolvedReturn = paramTypes.isEmpty() ? new TypeInfoClass(Object.class) : paramTypes.remove(0);
            }
            return "(%s) => %s".formatted(String.join(", ", formattedParam), new FormatterType(resolvedReturn).format(0, 0));
        }
    }

    public static void processFunctionalInterfaces(Set<Class<?>> globalClasses) {
        for (Class<?> clazz : globalClasses) {
            if (clazz.isInterface() && clazz.getAnnotation(FunctionalInterface.class) != null && !skippedSpecials.contains(clazz)) {
                //Functional interfaces has one and only one abstract method
                for (Method method : clazz.getMethods()) {
                    if (Modifier.isAbstract(method.getModifiers())) {
                        FormatterLambda formatter = new FormatterLambda(new MethodInfo(method, method.getDeclaringClass()));
                        NameResolver.putTypeFormatter(clazz, formatter::format);
                        break;
                    }
                }
            }
        }
    }

    public static void init() {
        //Skipping classes that are not reasonably to be a functional interface
        skippedSpecials.add(IngredientJS.class);
    }
}
