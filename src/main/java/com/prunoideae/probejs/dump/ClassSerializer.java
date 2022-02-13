package com.prunoideae.probejs.dump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.lang.reflect.*;
import java.util.Arrays;

public class ClassSerializer {
    private static JsonObject serializeType(Class<?> clazz, Type type) {
        JsonObject typeJson = new JsonObject();
        typeJson.addProperty("classname", clazz.getName());
        if (type instanceof ParameterizedType) {
            JsonArray paramsJson = new JsonArray();
            Arrays.stream(((ParameterizedType) type).getActualTypeArguments()).forEach(t -> paramsJson.add(t.getTypeName()));
            typeJson.add("parameterized", paramsJson);
        } else {
            typeJson.add("parameterized", new JsonArray());
        }
        return typeJson;
    }

    private static JsonArray serializeFields(Field[] fields) {
        JsonArray fieldsJson = new JsonArray();
        for (Field field : fields) {
            if (field.getAnnotation(HideFromJS.class) != null)
                continue;
            JsonObject fieldJson = new JsonObject();
            fieldJson.addProperty("name", field.getName());
            fieldJson.add("type", serializeType(field.getType(), field.getGenericType()));
            fieldJson.addProperty("static", Modifier.isStatic(field.getModifiers()));
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    if (field.canAccess(null))
                        KeySerializer.putPrimitiveSafe(fieldJson, "value", field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            fieldsJson.add(fieldJson);
        }
        return fieldsJson;
    }

    private static JsonArray serializeMethods(Method[] methods) {
        JsonArray methodsJson = new JsonArray();
        for (Method method : methods) {
            if (method.getAnnotation(HideFromJS.class) != null)
                continue;
            JsonObject methodJson = new JsonObject();
            methodJson.addProperty("name", method.getName());
            methodJson.addProperty("static", Modifier.isStatic(method.getModifiers()));
            methodJson.add("return_type", serializeType(method.getReturnType(), method.getGenericReturnType()));

            JsonArray paramsJson = new JsonArray();
            Arrays.stream(method.getParameters()).forEach(param -> {
                JsonObject paramJson = new JsonObject();
                paramJson.addProperty("name", param.getName());
                paramJson.add("type", serializeType(param.getType(), param.getParameterizedType()));
                paramsJson.add(paramJson);
            });
            methodJson.add("params", paramsJson);
            methodsJson.add(methodJson);
        }
        return methodsJson;
    }

    private static JsonArray serializeConstructors(Constructor<?>[] constructors) {
        JsonArray constructorsJson = new JsonArray();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(HideFromJS.class) != null)
                continue;
            JsonObject constructorJson = new JsonObject();
            JsonArray paramsJson = new JsonArray();
            Arrays.stream(constructor.getParameters()).forEach(param -> {
                JsonObject paramJson = new JsonObject();
                paramJson.addProperty("name", param.getName());
                paramJson.add("type", serializeType(param.getType(), param.getParameterizedType()));
                paramsJson.add(paramJson);
            });
            constructorJson.add("params", paramsJson);
            constructorsJson.add(constructorJson);
        }
        return constructorsJson;
    }

    public static JsonObject serializeClass(Class<?> clazz) {
        JsonObject classJson = new JsonObject();

        classJson.addProperty("name", clazz.getName());
        classJson.addProperty("allowed", ServerScriptManager.instance.scriptManager.isClassAllowed(clazz.getName()));
        Class<?> s = clazz.getSuperclass();
        if (s != null && s != clazz)
            classJson.addProperty("super", s.getName());
        classJson.add("fields", serializeFields(clazz.getFields()));
        classJson.add("methods", serializeMethods(clazz.getMethods()));
        classJson.add("constructors", serializeConstructors(clazz.getConstructors()));
        return classJson;
    }
}
