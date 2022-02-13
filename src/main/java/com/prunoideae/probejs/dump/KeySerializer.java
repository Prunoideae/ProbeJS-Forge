package com.prunoideae.probejs.dump;

import com.google.gson.JsonObject;

public class KeySerializer {
    private static String native2ascii(char ch) {
        StringBuilder sb = new StringBuilder();
        // write \udddd
        sb.append("\\u");
        StringBuilder hex = new StringBuilder(Integer.toHexString(ch));
        hex.reverse();
        int length = 4 - hex.length();
        hex.append("0".repeat(Math.max(0, length)));
        for (int j = 0; j < 4; j++) {
            sb.append(hex.charAt(3 - j));
        }
        return sb.toString();
    }

    public static void putPrimitiveSafe(JsonObject object, String key, Object value) {
        if (value == null) {
            object.addProperty(key, (String) null);
            return;
        }

        if (value instanceof Character) {
            object.addProperty(key, native2ascii((Character) value));
        } else if (value instanceof Float) {
            if (Float.isNaN((Float) value)) {
                object.addProperty(key, "NaN");
            } else if (Float.isInfinite((Float) value)) {
                if ((Float) value == Float.POSITIVE_INFINITY)
                    object.addProperty(key, "inf");
                else
                    object.addProperty(key, "-inf");
            } else
                object.addProperty(key, (Number) value);
        } else if (value instanceof Double) {
            if (Double.isNaN((Double) value)) {
                object.addProperty(key, "NaN");
            } else if (Double.isInfinite((Double) value)) {
                if ((Double) value == Double.POSITIVE_INFINITY)
                    object.addProperty(key, "inf");
                else
                    object.addProperty(key, "-inf");
            } else
                object.addProperty(key, (Number) value);
        } else if (value instanceof Number) {
            object.addProperty(key, (Number) value);
        } else if (value instanceof Boolean) {
            object.addProperty(key, (Boolean) value);
        } else if (value instanceof String) {
            object.addProperty(key, (String) value);
        } else if (value instanceof Enum) {
            object.addProperty(key, ((Enum<?>) value).name());
        } else if (value instanceof Class) {
            object.addProperty(key, ((Class<?>) value).getName());
        }
    }


}
