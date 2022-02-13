package com.prunoideae.probejs.dump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

public class ProcessBinding {

    private static boolean isPrimitive(Object object) {
        return object instanceof Byte ||
                object instanceof Short ||
                object instanceof Integer ||
                object instanceof Long ||
                object instanceof Float ||
                object instanceof Double ||
                object instanceof Boolean ||
                object instanceof Character ||
                object instanceof String ||
                object instanceof Enum;
    }

    public static JsonArray fetchConstantData(DummyBindingEvent event) {
        JsonArray constants = new JsonArray();
        for (Map.Entry<String, Object> entry : event.getConstantDumpMap().entrySet()) {
            JsonObject constantJson = new JsonObject();
            KeySerializer.putPrimitiveSafe(constantJson, "name", entry.getKey());
            KeySerializer.putPrimitiveSafe(constantJson, "classname", entry.getValue().getClass());
            KeySerializer.putPrimitiveSafe(constantJson, "value", entry.getValue());
            constants.add(constantJson);
        }
        return constants;
    }

    public static JsonArray fetchClassData(DummyBindingEvent event) {
        JsonArray classesJson = new JsonArray();
        event.getClassDumpMap().forEach((key, value) -> {
            JsonObject classJson = new JsonObject();
            classJson.addProperty("classname", value.getName());
            classJson.addProperty("name", key);
            classesJson.add(classJson);
        });
        return classesJson;
    }

    public static JsonArray fetchFunctionData(DummyBindingEvent event) {
        JsonArray array = new JsonArray();
        event.getFunctionDump().keySet().forEach(array::add);
        return array;
    }
}
