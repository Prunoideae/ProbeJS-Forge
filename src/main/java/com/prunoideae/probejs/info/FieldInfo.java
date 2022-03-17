package com.prunoideae.probejs.info;

import com.prunoideae.probejs.info.type.ITypeInfo;
import com.prunoideae.probejs.info.type.InfoTypeResolver;
import com.prunoideae.probejs.util.Util;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;

public class FieldInfo {
    private final String name;
    private final int modifiers;
    private final boolean shouldHide;
    private final Object value;
    private ITypeInfo info;

    public FieldInfo(Field field) {
        name = field.getAnnotation(RemapForJS.class) != null ? field.getAnnotation(RemapForJS.class).value() : field.getName();
        modifiers = field.getModifiers();
        shouldHide = field.getAnnotation(HideFromJS.class) != null;
        info = InfoTypeResolver.resolveType(field.getGenericType());
        value = Util.tryOrDefault(() -> isStatic() ? field.get(null) : null, null);
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public String getName() {
        return name;
    }

    public boolean shouldHide() {
        return shouldHide;
    }

    public ITypeInfo getType() {
        return info;
    }

    public Object getStaticValue() {
        return value;
    }

    public void setTypeInfo(ITypeInfo info) {
        this.info = info;
    }
}
