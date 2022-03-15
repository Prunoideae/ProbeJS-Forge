package com.prunoideae.probejs.info;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class FieldInfo {
    private final Field field;

    public FieldInfo(Field field) {
        this.field = field;
    }

    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    public String getName() {
        if (field.getAnnotation(RemapForJS.class) != null)
            return field.getAnnotation(RemapForJS.class).value();
        return this.field.getName();
    }

    public boolean shouldHide() {
        return field.getAnnotation(HideFromJS.class) != null;
    }

    public TypeInfo getType() {
        return new TypeInfo(field.getGenericType());
    }

    public Object getStaticValue() {
        if (!isStatic())
            return null;
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
