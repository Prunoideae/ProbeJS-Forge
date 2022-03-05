package com.prunoideae.probejs.typings;

import com.mojang.datafixers.util.Pair;
import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.resolver.document.Document;
import com.prunoideae.probejs.resolver.document.DocumentManager;
import com.prunoideae.probejs.resolver.document.info.ClassDocument;
import com.prunoideae.probejs.resolver.document.info.FieldDocument;
import com.prunoideae.probejs.resolver.document.info.MethodDocument;
import com.prunoideae.probejs.toucher.ClassInfo;
import dev.latvian.mods.kubejs.recipe.RecipeEventJS;
import it.unimi.dsi.fastutil.Hash;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TSGlobalClassFormatter {
    public static HashMap<Class<?>, Function<ClassInfo.TypeInfo, String>> specialTypeFormatter = new HashMap<>();
    public static HashMap<Class<?>, Class<? extends ClassFormatter>> specialClassFormatter = new HashMap<>();
    public static HashMap<Class<?>, Function<Object, String>> staticValueTransformer = new HashMap<>();
    public static HashMap<String, String> resolvedClassName = new HashMap<>();

    public static String transformValue(Object object) {
        if (staticValueTransformer.containsKey(object.getClass()))
            return staticValueTransformer.get(object.getClass()).apply(object);
        for (Map.Entry<Class<?>, Function<Object, String>> entry : staticValueTransformer.entrySet()) {
            if (entry.getKey().isAssignableFrom(object.getClass()))
                return entry.getValue().apply(object);
        }
        return null;
    }

    private static String getCamelCase(String text) {
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

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

    public static String serializeType(Type type) {
        if (type instanceof TypeVariable)
            return ((TypeVariable<?>) type).getName();
        if (type instanceof WildcardType) {
            if (((WildcardType) type).getLowerBounds().length != 0)
                return serializeType(((WildcardType) type).getLowerBounds()[0]);
            if (((WildcardType) type).getUpperBounds()[0] != Object.class)
                return serializeType(((WildcardType) type).getUpperBounds()[0]);
            return "any";
        }
        if (type instanceof ParameterizedType) {
            return serializeType(((ParameterizedType) type).getRawType()) + "<%s>".formatted(Arrays.stream(((ParameterizedType) type).getActualTypeArguments()).map(TSGlobalClassFormatter::serializeType).collect(Collectors.joining(", ")));
        }
        if (type instanceof GenericArrayType) {
            return serializeType(((GenericArrayType) type).getGenericComponentType()) + "[]";
        }
        int depth = 0;
        if (type instanceof Class) {
            if (((Class<?>) type).isArray()) {
                Pair<Class<?>, Integer> pair = getClassOrComponent((Class<?>) type);
                depth = pair.getSecond();
                type = pair.getFirst();
            }
        }
        String[] clzPath = type.getTypeName().split("\\.");
        String name = clzPath[clzPath.length - 1];
        if (name.contains("/"))
            name = name.split("/")[0];
        return resolvedClassName.getOrDefault(type.getTypeName(), "Unknown." + name) + "[]".repeat(depth);
    }

    public static class TypeFormatter implements ITSFormatter {
        protected final ClassInfo.TypeInfo typeInfo;

        public TypeFormatter(ClassInfo.TypeInfo typeInfo) {
            this.typeInfo = typeInfo;
        }

        @Override
        public String format() {
            Class<?> clazz = this.typeInfo.getTypeClass();
            String resolvedType = specialTypeFormatter.containsKey(clazz)
                    ? specialTypeFormatter.get(clazz).apply(this.typeInfo)
                    : serializeType(this.typeInfo.getType());
            //Ensure to resolve type since TS does not allow non-parameterized types.
            if (this.typeInfo.getType() instanceof Class<?> type) {
                if (type.getTypeParameters().length != 0)
                    resolvedType += "<%s>".formatted(String.join(",", Collections.nCopies(type.getTypeParameters().length, "unknown")));
            }
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
        private final MethodDocument document;

        public MethodFormatter(ClassInfo.MethodInfo methodInfo) {
            this(methodInfo, null);
        }

        public MethodFormatter(ClassInfo.MethodInfo methodInfo, MethodDocument document) {
            this.methodInfo = methodInfo;
            this.document = document;
            if (document != null)
                ProbeJS.LOGGER.info("SUCCESS");
        }

        @Override
        public String format() {
            HashMap<String, String> paramModifiers = new HashMap<>();
            
            if (document != null && document.getCommentDocument() != null)
                paramModifiers.putAll(document.getCommentDocument().getParamModifiers());
            Set<String> usedModifiers = new HashSet<>();

            String formattedParams = this.methodInfo
                    .getParamsInfo()
                    .stream()
                    .map(paramInfo -> {
                        if (document == null || document.getCommentDocument() == null)
                            return new ParameterFormater(paramInfo).format();
                        String modifiedType = paramModifiers.get(paramInfo.getName());
                        if (modifiedType == null)
                            return new ParameterFormater(paramInfo).format();
                        usedModifiers.add(paramInfo.getName());
                        return modifiedType.equals("null") ? null : "%s: %s".formatted(paramInfo.getName(), Document.formatType(modifiedType));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            String modifierParams = paramModifiers.entrySet().stream().filter(e -> !usedModifiers.contains(e.getKey())).map(e -> "%s: %s".formatted(e.getKey(), Document.formatType(e.getValue()))).collect(Collectors.joining(", "));

            if (formattedParams.isEmpty())
                formattedParams = modifierParams;
            else if (!modifierParams.isEmpty())
                formattedParams = formattedParams + ", " + modifierParams;

            String formatted = "%s(%s): %s;".formatted(
                    this.methodInfo.getName(),
                    formattedParams,
                    (document == null || document.getCommentDocument() == null || document.getCommentDocument().getReturnTypeModifier() == null)
                            ? new TypeFormatter(this.methodInfo.getReturnTypeInfo()).format()
                            : Document.formatType(document.getCommentDocument().getReturnTypeModifier()));
            if (this.methodInfo.isStatic())
                formatted = "static " + formatted;
            return formatted;
        }
    }

    public static class FieldFormatter implements ITSFormatter {
        private final ClassInfo.FieldInfo fieldInfo;

        public static String formatValue(Object obj) {
            if (obj == null)
                return "any";
            return transformValue(obj);
        }

        public FieldFormatter(ClassInfo.FieldInfo fieldInfo) {
            this.fieldInfo = fieldInfo;
        }

        @Override
        public String format() {
            String resolvedAnnotation = null;
            if (this.fieldInfo.isStatic() && this.fieldInfo.getStaticValue() != null) {
                Object value = this.fieldInfo.getStaticValue();
                resolvedAnnotation = formatValue(value);
            }
            if (resolvedAnnotation == null) {
                resolvedAnnotation = new TypeFormatter(this.fieldInfo.getTypeInfo()).format();
            } else {
                resolvedAnnotation += " & " + new TypeFormatter(this.fieldInfo.getTypeInfo()).format();
            }

            String formatted = "%s: %s;".formatted(this.fieldInfo.getName(), resolvedAnnotation);
            if (this.fieldInfo.isStatic())
                formatted = "static " + formatted;
            if (this.fieldInfo.isFinal())
                formatted = "readonly " + formatted;
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
        private final List<ClassDocument> documents;


        public ClassFormatter(ClassInfo classInfo, Integer indentation, Integer stepIndentation, Predicate<String> namePredicate, boolean useSpecialFormatters) {
            this.classInfo = classInfo;
            this.indentation = indentation;
            this.stepIndentation = stepIndentation;
            this.namePredicate = namePredicate;
            this.useSpecialFormatters = useSpecialFormatters;
            this.documents = DocumentManager.classModifiers.getOrDefault(classInfo.getClazz().getName(), new ArrayList<>());
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
            /*
             * Criteria: tranfsorm .get?() methods to .? fields.
             * If an field of same name exist, ignore it.
             * .get() beans will be readonly, having a .set() will remove the readonly statement.
             * Only having a .set() will NOT make this write-only as TS does not have such a thing.
             * */

            Set<ClassInfo.MethodInfo> beaned = new HashSet<>();
            Set<String> existedNames = classInfo.getFields().stream().map(ClassInfo.FieldInfo::getName).collect(Collectors.toSet());
            Set<String> overriddenNames = new HashSet<>();
            this.documents.forEach(document -> document.getFieldDocuments().forEach(fieldDocument -> overriddenNames.add(fieldDocument.getName())));
            existedNames.addAll(classInfo.getMethods().stream().map(ClassInfo.MethodInfo::getName).collect(Collectors.toSet()));

            //Add class getters/setters to beaned
            classInfo.getMethods()
                    .stream()
                    .filter(methodInfo -> !(methodInfo.getName().equals("get")))
                    .filter(methodInfo -> methodInfo.getName().startsWith("get"))
                    .filter(methodInfo -> !Character.isDigit(methodInfo.getName().substring(3).charAt(0)))
                    .filter(methodInfo -> !existedNames.contains(getCamelCase(methodInfo.getName().substring(3))))
                    .filter(methodInfo -> methodInfo.getParamsInfo().size() == 0)
                    .forEach(beaned::add);

            classInfo.getMethods()
                    .stream()
                    .filter(methodInfo -> !methodInfo.getName().equals("set"))
                    .filter(methodInfo -> methodInfo.getName().startsWith("set"))
                    .filter(methodInfo -> !Character.isDigit(methodInfo.getName().substring(3).charAt(0)))
                    .filter(methodInfo -> methodInfo.getParamsInfo().size() == 1)
                    .filter(methodInfo -> !existedNames.contains(getCamelCase(methodInfo.getName().substring(3))))
                    .forEach(beaned::add);

            //Iterate through getter/setters, generate beans.
            Set<String> beanedNames = new HashSet<>();
            HashMap<String, ClassInfo.MethodInfo> cachedGetterNames = new HashMap<>();
            beaned.forEach(methodInfo -> {
                String beanName = getCamelCase(methodInfo.getName().substring(3));
                if (overriddenNames.contains(beanName))
                    return;
                if (methodInfo.getName().startsWith("get")) {
                    if (beanedNames.contains(beanName))
                        return;
                    cachedGetterNames.put(beanName, methodInfo);
                } else if (methodInfo.getName().startsWith("set")) {
                    beanedNames.add(beanName);
                    cachedGetterNames.remove(beanName);
                    Type type = methodInfo.getParamsInfo().get(0).getType();
                    String resolved = serializeType(type);
                    if (type instanceof Class<?> clazz) {
                        if (clazz.getTypeParameters().length != 0)
                            resolved += "<%s>".formatted(String.join(",", Collections.nCopies(clazz.getTypeParameters().length, "unknown")));
                    }
                    String formatted = "%s: %s;".formatted(beanName, resolved);
                    if (methodInfo.isStatic())
                        formatted = "static " + formatted;
                    innerLines.add(" ".repeat(linesIdent) + formatted);
                }
            });

            classInfo.getMethods()
                    .stream()
                    .filter(methodInfo -> !methodInfo.getName().equals("is"))
                    .filter(methodInfo -> methodInfo.getName().startsWith("is"))
                    .filter(methodInfo -> !Character.isDigit(methodInfo.getName().substring(2).charAt(0)))
                    .filter(methodInfo -> !existedNames.contains(getCamelCase(methodInfo.getName().substring(2))))
                    .filter(methodInfo -> methodInfo.getParamsInfo().size() == 0)
                    .filter(methodInfo -> {
                        Type type = methodInfo.getReturnTypeInfo().getType();
                        return type instanceof Class && (type == Boolean.TYPE || type == Boolean.class);
                    })
                    .filter(methodInfo -> !beanedNames.contains(getCamelCase(methodInfo.getName().substring(2))))
                    .forEach(methodInfo -> {
                        beaned.add(methodInfo);
                        String formatted = "%s: boolean;".formatted(getCamelCase(methodInfo.getName().substring(2)));
                        if (methodInfo.isStatic())
                            formatted = "static " + formatted;
                        formatted = "readonly " + formatted;
                        innerLines.add(" ".repeat(linesIdent) + formatted);
                    });


            cachedGetterNames.forEach((k, v) -> {
                Type type = v.getReturnTypeInfo().getType();
                String resolved = serializeType(v.getReturnTypeInfo().getType());
                if (type instanceof Class<?> clazz) {
                    if (clazz.getTypeParameters().length != 0)
                        resolved += "<%s>".formatted(String.join(",", Collections.nCopies(clazz.getTypeParameters().length, "any")));
                }
                if (v.isStatic()) {
                    try {
                        String value = transformValue(v.getMethod().invoke(null));
                        if (value != null)
                            resolved = value + " & " + resolved;
                    } catch (Exception e) {
                        ProbeJS.LOGGER.warn("Unable to get static value of bean %s, falling back to type.".formatted(k));
                    }
                }
                String formatted = "%s: %s;".formatted(k, resolved);
                if (v.isStatic())
                    formatted = "static " + formatted;
                formatted = "readonly " + formatted;
                innerLines.add(" ".repeat(linesIdent) + formatted);
            });

            HashMap<String, List<MethodDocument>> documentsOfMethod = new HashMap<>();
            documents.forEach(classDoc -> classDoc.getMethodDocuments().forEach(method -> documentsOfMethod.computeIfAbsent(method.getName(), o -> new ArrayList<>()).add(method)));

            classInfo.getMethods()
                    .stream()
                    .filter(methodInfo -> this.namePredicate.test(methodInfo.getName()))
                    .filter(methodInfo -> !beaned.contains(methodInfo))
                    .forEach(methodInfo -> {
                        MethodDocument methodDocument = null;
                        List<MethodDocument> documentsList = documentsOfMethod.get(methodInfo.getName());
                        if (documentsList != null) {
                            methodDocument = documentsList.stream().filter(m -> m.isMatch(methodInfo)).findFirst().orElse(null);
                        }
                        if (methodDocument != null && methodDocument.getCommentDocument() != null)
                            innerLines.addAll(methodDocument.getCommentDocument().getCommentText(linesIdent));
                        innerLines.add(" ".repeat(linesIdent) + new MethodFormatter(methodInfo, methodDocument).format());
                    });
            return innerLines;
        }

        protected List<String> compileFields(Set<String> usedMethod) {
            HashMap<String, FieldDocument> fieldDocuments = new HashMap<>();
            documents.forEach(document -> document.getFieldDocuments().forEach(fieldDocument -> fieldDocuments.put(fieldDocument.getName(), fieldDocument)));
            int linesIndent = this.indentation + stepIndentation;
            List<String> innerLines = new ArrayList<>();
            classInfo.getFields()
                    .stream()
                    .filter(fieldInfo -> !fieldDocuments.containsKey(fieldInfo.getName()))
                    .filter(fieldInfo -> !usedMethod.contains(fieldInfo.getName()))
                    .filter(fieldInfo -> this.namePredicate.test(fieldInfo.getName()))
                    .forEach(fieldInfo -> innerLines.add(" ".repeat(linesIndent) + new FieldFormatter(fieldInfo).format()));
            fieldDocuments.forEach((s, fieldDocument) -> innerLines.addAll(fieldDocument.format(linesIndent)));
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
            this.documents.stream()
                    .filter(document -> document.getComment() != null)
                    .findFirst()
                    .ifPresent(commentedDocument -> innerLines.addAll(commentedDocument.getComment().getCommentText(this.indentation)));

            //Compile the first line of class declaration
            String[] classPath = resolvedClassName.get(this.classInfo.getClazz().getName()).split("\\.");
            String firstLine = "%s%s %s%s ".formatted(
                    " ".repeat(this.indentation),
                    this.classInfo.getClazz().isInterface() ? "interface" : "class",
                    classPath[classPath.length - 1],
                    (this.classInfo.getClazz().getTypeParameters().length != 0
                            ? "<" + Arrays.stream(this.classInfo.getClazz().getTypeParameters()).map(Type::getTypeName).collect(Collectors.joining(", ")) + ">"
                            : ""));
            if (classInfo.getSuperTypes().size() != 0) {
                firstLine += "extends %s".formatted(classInfo.getSuperTypes().stream().map(TSGlobalClassFormatter::serializeType).collect(Collectors.joining(", ")));
            }
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
