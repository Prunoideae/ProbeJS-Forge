package com.prunoideae.probejs.bytecode;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

public class MethodInsnNodeFilter implements Predicate<AbstractInsnNode> {
    //signatures
    private String name;
    private String desc;
    private int opCode;
    //params
    private int insnLookOffset;

    public MethodInsnNodeFilter(String name, String desc, int opCode, int insnLookOffset) {
        this.name = name;
        this.desc = desc;
        this.opCode = opCode;
        this.insnLookOffset = insnLookOffset;
    }

    @Override
    public boolean test(AbstractInsnNode node) {
        return (node instanceof MethodInsnNode) &&
            opCode == node.getOpcode() &&
            Objects.equals(name, ((MethodInsnNode) node).name) &&
            Objects.equals(desc, ((MethodInsnNode) node).desc);

    }

    public int getInsnLookOffset() {
        return insnLookOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInsnNodeFilter that = (MethodInsnNodeFilter) o;
        return opCode == that.opCode && insnLookOffset == that.insnLookOffset && Objects.equals(name, that.name) && Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, opCode, insnLookOffset);
    }
}
