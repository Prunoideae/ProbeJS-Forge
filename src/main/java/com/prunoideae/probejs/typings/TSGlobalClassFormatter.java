package com.prunoideae.probejs.typings;

import com.mojang.datafixers.util.Pair;
import com.prunoideae.probejs.toucher.ClassInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TSGlobalClassFormatter {
    public static HashMap<Class<?>, Function<ClassInfo.TypeInfo, String>> specialTypeFormatter = new HashMap<>();
    public static HashMap<Class<?>, Class<? extends ClassFormatter>> specialClassFormatter = new HashMap<>();
    public static HashMap<Class<?>, Function<Object, String>> staticValueTransformer = new HashMap<>();
    public static HashMap<Class<?>, String> resolvedClassName = new HashMap<>();

    private static Pair<Class<?>, Integer> getClassOrComponent(Class<?> clazz) {
        if (clazz == null)
            return null;
        int depth = 0;
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
            depth++;
        }
        return new Pair<>(clazz, depth);
    }

    public static class TypeFormatter implements ITSFormatter {
        protected final ClassInfo.TypeInfo typeInfo;

        public TypeFormatter(ClassInfo.TypeInfo typeInfo) {
            this.typeInfo = typeInfo;
        }

        @Override
        public String format() {
            Class<?> clazz = this.typeInfo.getTypeClass();
            int arrayDepth = -1;
            if (clazz != null && clazz.isArray()) {
                Pair<Class<?>, Integer> pair = getClassOrComponent(clazz);
                clazz = pair.getFirst();
                arrayDepth = pair.getSecond();
            }
            String resolvedType = (specialTypeFormatter.containsKey(clazz)
                    ? specialTypeFormatter.get(clazz).apply(this.typeInfo)
                    : resolvedClassName.get(clazz));
            if (arrayDepth != -1)
                resolvedType += "[]".repeat(arrayDepth);
            return resolvedType;
        }
    }

    public static class ParameterFormater extends TypeFormatter {
        public ParameterFormater(ClassInfo.ParamInfo paramInfo) {
            super(paramInfo);
        }

        @Override
        public String format() {
            return "%s: %s".formatted(NameGuard.getSafeName(((ClassInfo.ParamInfo) this.typeInfo).getName()), super.format());
        }
    }

    public static class MethodFormatter implements ITSFormatter {
        private final ClassInfo.MethodInfo methodInfo;

        public MethodFormatter(ClassInfo.MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
        }

        @Override
        public String format() {
            String formatted = "%s(%s): %s;".formatted(
                    this.methodInfo.getName(),
                    this.methodInfo
                            .getParamsInfo()
                            .stream()
                            .map(ParameterFormater::new)
                            .map(ParameterFormater::format)
                            .collect(Collectors.joining(", ")),
                    new TypeFormatter(this.methodInfo.getReturnTypeInfo()).format());
            if (this.methodInfo.isStatic())
                formatted = "static " + formatted;
            return formatted;
        }
    }

    public static class FieldFormatter implements ITSFormatter {
        private final ClassInfo.FieldInfo fieldInfo;

        public FieldFormatter(ClassInfo.FieldInfo fieldInfo) {
            this.fieldInfo = fieldInfo;
        }

        @Override
        public String format() {
            String resolvedAnnotation;
            if (this.fieldInfo.isStatic()
                    && this.fieldInfo.getStaticValue() != null
                    && staticValueTransformer.containsKey(this.fieldInfo.getStaticValue().getClass())) {
                Object value = this.fieldInfo.getStaticValue();
                resolvedAnnotation = staticValueTransformer.get(value.getClass()).apply(value);
            } else {
                resolvedAnnotation = new TypeFormatter(this.fieldInfo.getTypeInfo()).format();
            }

            String formatted = "%s: %s;".formatted(this.fieldInfo.getName(), resolvedAnnotation);
            if (this.fieldInfo.isStatic())
                formatted = "static " + formatted;
            return formatted;
        }
    }

    public static class ConstructorFormatter implements ITSFormatter {
        private final ClassInfo.ConstructorInfo constructorInfo;

        public ConstructorFormatter(ClassInfo.ConstructorInfo constructorInfo) {
            this.constructorInfo = constructorInfo;
        }

        @Override
        public String format() {
            return "constructor(%s);".formatted(
                    this.constructorInfo
                            .getParamsInfo()
                            .stream()
                            .map(ParameterFormater::new)
                            .map(ParameterFormater::format)
                            .collect(Collectors.joining(", ")));
        }
    }

    public static class ClassFormatter implements ITSFormatter {
        private final ClassInfo classInfo;
        protected Integer indentation;
        protected Integer stepIndentation;
        private Predicate<String> namePredicate;
        private final boolean useSpecialFormatters;


        public ClassFormatter(ClassInfo classInfo, Integer indentation, Integer stepIndentation, Predicate<String> namePredicate, boolean useSpecialFormatters) {
            this.classInfo = classInfo;
            this.indentation = indentation;
            this.stepIndentation = stepIndentation;
            this.namePredicate = namePredicate;
            this.useSpecialFormatters = useSpecialFormatters;
        }

        public ClassFormatter(ClassInfo classInfo, Integer indentation, Integer stepIndentation, Predicate<String> namePredicate) {
            this(classInfo, indentation, stepIndentation, namePredicate, true);
        }

        public ClassFormatter(ClassInfo classInfo, Integer indentation, Integer stepIndentation) {
            this(classInfo, indentation, stepIndentation, (s) -> true);
        }

        public ClassFormatter(ClassInfo classInfo) {
            this(classInfo, 0, 4);
        }

        public ClassFormatter setIdent(Integer ident) {
            this.indentation = ident;
            return this;
        }

        public ClassFormatter setStepIndentation(Integer indentation) {
            this.stepIndentation = indentation;
            return this;
        }

        public ClassFormatter setNameGuard(Predicate<String> predicate) {
            this.namePredicate = predicate;
            return this;
        }

        protected List<String> compileMethods() {
            int linesIdent = this.indentation + stepIndentation;
            List<String> innerLines = new ArrayList<>();
            classInfo.getMethods()
                    .stream()
                    .filter(methodInfo -> this.namePredicate.test(methodInfo.getName()))
                    .forEach(methodInfo -> innerLines.add(" ".repeat(linesIdent) + new MethodFormatter(methodInfo).format()));
            return innerLines;
        }

        protected List<String> compileFields(Set<String> usedMethod) {
            int linesIdent = this.indentation + stepIndentation;
            List<String> innerLines = new ArrayList<>();
            classInfo.getFields()
                    .stream()
                    .filter(fieldInfo -> !usedMethod.contains(fieldInfo.getName()))
                    .filter(fieldInfo -> this.namePredicate.test(fieldInfo.getName()))
                    .forEach(fieldInfo -> innerLines.add(" ".repeat(linesIdent) + new FieldFormatter(fieldInfo).format()));
            return innerLines;
        }

        protected List<String> compileConstructors() {
            int linesIdent = this.indentation + stepIndentation;
            List<String> innerLines = new ArrayList<>();
            classInfo.getConstructors().forEach(constructorInfo -> innerLines.add(" ".repeat(linesIdent) + new ConstructorFormatter(constructorInfo).format()));
            return innerLines;
        }

        @Override
        public String format() {
            //Turn to special class formatters if there is one, for patching special classes.
            if (this.useSpecialFormatters && specialClassFormatter.containsKey(this.classInfo.getClazz())) {
                try {
                    return specialClassFormatter
                            .get(this.classInfo.getClazz())
                            .getDeclaredConstructor(ClassInfo.class, Integer.class, Integer.class, Predicate.class)
                            .newInstance(this.classInfo, this.indentation, this.stepIndentation, this.namePredicate)
                            .format();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            List<String> innerLines = new ArrayList<>();

            //Compile the first line of class declaration
            String[] classPath = resolvedClassName.get(this.classInfo.getClazz()).split("\\.");
            String firstLine = "%sclass %s ".formatted(" ".repeat(this.indentation), classPath[classPath.length - 1]);
            if (classInfo.getSuperClass() != null)
                firstLine += "extends %s ".formatted(resolvedClassName.get(classInfo.getSuperClass()));
            innerLines.add(firstLine + "{");

            //Compile methods, fields and constructors
            //Method names overrides field names if conflict

            //Compile methods
            Set<String> usedMethod = this.classInfo.getMethods().stream().map(ClassInfo.MethodInfo::getName).filter(this.namePredicate).collect(Collectors.toSet());
            innerLines.addAll(this.compileMethods());

            //Compile fields
            innerLines.addAll(this.compileFields(usedMethod));

            //Compile constructors
            innerLines.addAll(this.compileConstructors());

            //Add the bracket
            innerLines.add(" ".repeat(this.indentation) + "}\n");
            return String.join("\n", innerLines);
        }
    }

    public static class NamespaceFormatter implements ITSFormatter {
        private final List<ClassFormatter> classInfos;
        private final String classPath;
        private final int stepIndentation;
        private final int indentation;
        private final boolean export;

        public NamespaceFormatter(String classPath, List<ClassFormatter> classInfos, int stepIndentation, int indentation, boolean export) {
            this.classPath = classPath;
            this.classInfos = classInfos;
            this.stepIndentation = stepIndentation;
            this.indentation = indentation;
            this.export = export;
        }

        @Override
        public String format() {
            this.classInfos.forEach(
                    classFormatter -> classFormatter
                            .setIdent(this.indentation + this.stepIndentation)
                            .setStepIndentation(this.stepIndentation));
            return (export ? "declare " : "") + "namespace %s {\n%s}\n".formatted(this.classPath, this.classInfos.stream().map(ClassFormatter::format).collect(Collectors.joining("")));
        }
    }
}
