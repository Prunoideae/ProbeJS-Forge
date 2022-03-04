package com.prunoideae.probejs.toucher;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public final class ClassInfo {
    private final Class<?> clazz;

    public ClassInfo(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public static class TypeInfo {
        private final Type type;
        private final Class<?> classType;

        public TypeInfo(Type type, Class<?> classType) {
            this.type = type;
            this.classType = classType;
        }

        public Class<?> getTypeClass() {
            return classType;
        }

        public Type getType() {
            return type;
        }

        public List<Type> getTypeArguments() {
            if (this.type instanceof ParameterizedType) {
                return Arrays.stream(((ParameterizedType) this.type).getActualTypeArguments()).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

        @Override
        public String toString() {
            return "TypeInfo[" +
                    "type=" + type + ", " +
                    "classType=" + classType + ']';
        }

    }

    public static class ParamInfo extends TypeInfo {
        private final Parameter parameter;

        public ParamInfo(Parameter parameter) {
            super(parameter.getParameterizedType(), parameter.getType());
            this.parameter = parameter;
        }

        public String getName() {
            return this.parameter.getName();
        }

        @Override
        public String toString() {
            return "ParamInfo[" +
                    "parameter=" + parameter + ']';
        }

    }

    public static class MethodInfo {
        private final Method method;

        public MethodInfo(Method method) {
            this.method = method;
        }

        public String getName() {
            return this.method.getName();
        }

        public boolean isStatic() {
            return Modifier.isStatic(this.method.getModifiers());
        }

        public List<ParamInfo> getParamsInfo() {
            return Arrays.stream(this.method.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
        }

        public TypeInfo getReturnTypeInfo() {
            return new TypeInfo(this.method.getGenericReturnType(), this.method.getReturnType());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodInfo that = (MethodInfo) o;
            return method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method);
        }

        @Override
        public String toString() {
            return "MethodInfo[" +
                    "method=" + method + ']';
        }

    }

    public static class ConstructorInfo {
        private final Constructor<?> constructor;

        public ConstructorInfo(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        public List<ParamInfo> getParamsInfo() {
            return Arrays.stream(this.constructor.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "ConstructorInfo[" +
                    "constructor=" + constructor + ']';
        }

    }

    public static class FieldInfo {
        private final Field field;

        public FieldInfo(Field field) {
            this.field = field;
        }

        public String getName() {
            return this.field.getName();
        }

        public boolean isStatic() {
            return Modifier.isStatic(this.field.getModifiers());
        }

        public boolean isFinal() {
            return Modifier.isFinal(this.field.getModifiers());
        }

        public TypeInfo getTypeInfo() {
            return new TypeInfo(this.field.getGenericType(), this.field.getType());
        }

        public Object getStaticValue() {
            if (!this.isStatic())
                throw new UnsupportedOperationException("Can not access default value of non-static fields");
            try {
                return this.field.get(null);
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "FieldInfo[" +
                    "field=" + field + ']';
        }

    }

    public List<MethodInfo> getMethods() {
        Set<Method> superMethod = new HashSet<>();
        this.getSuperClass().forEach(cls -> superMethod.addAll(List.of(cls.getMethods())));
        return Arrays.stream(this.clazz.getMethods()).filter(method -> !superMethod.contains(method)).map(MethodInfo::new).collect(Collectors.toList());
    }

    public List<ConstructorInfo> getConstructors() {
        return Arrays.stream(this.clazz.getConstructors()).map(ConstructorInfo::new).collect(Collectors.toList());
    }

    public List<FieldInfo> getFields() {
        return Arrays.stream(this.clazz.getFields()).map(FieldInfo::new).collect(Collectors.toList());
    }

    public List<Class<?>> getSuperClass() {
        List<Class<?>> classes = new ArrayList<>();
        if (!this.clazz.isInterface())
            classes.add(clazz.getSuperclass());
        classes.addAll(List.of(clazz.getInterfaces()));
        classes.removeIf(Objects::isNull);
        return classes;
    }

    public List<Type> getSuperTypes() {
        List<Type> types = new ArrayList<>();
        if (!this.clazz.isInterface() && this.clazz.getSuperclass() != Object.class)
            types.add(this.clazz.getGenericSuperclass());
        types.addAll(List.of(this.clazz.getGenericInterfaces()));
        types.removeIf(Objects::isNull);
        return types;
    }

    @Override
    public String toString() {
        return "ClassInfo[" +
                "clazz=" + clazz + ']';
    }

}
