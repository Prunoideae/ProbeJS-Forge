package com.prunoideae.probejs.document.type;

public class TypeUnion implements IType {
    private final IType leftType;
    private final IType rightType;

    public TypeUnion(IType leftType, IType rightType) {
        this.leftType = leftType;
        this.rightType = rightType;
    }

    @Override
    public String getTypeName() {
        return leftType.getTypeName() + " | " + rightType.getTypeName();
    }
}
