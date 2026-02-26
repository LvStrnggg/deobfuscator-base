package dev.lvstrng.deobfuscator.base.analysis.interpreter;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

@SuppressWarnings("all")
public class TypedValue implements Value {
    public static final TypedValue UNINITIALIZED_VALUE = new TypedValue(null);
    public static final TypedValue INT_VALUE = new TypedValue(Type.INT_TYPE);
    public static final TypedValue FLOAT_VALUE = new TypedValue(Type.FLOAT_TYPE);
    public static final TypedValue LONG_VALUE = new TypedValue(Type.LONG_TYPE);
    public static final TypedValue DOUBLE_VALUE = new TypedValue(Type.DOUBLE_TYPE);
    public static final TypedValue RETURNADDRESS_VALUE = new TypedValue(Type.VOID_TYPE);

    private final Type type;

    public TypedValue(final Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int getSize() {
        return type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ? 2 : 1;
    }

    public boolean isReference() {
        return type != null && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
    }

    @Override
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        } else if (value instanceof TypedValue) {
            if (type == null) {
                return ((TypedValue) value).type == null;
            } else {
                return type.equals(((TypedValue) value).type);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return type == null ? 0 : type.hashCode();
    }

    @Override
    public String toString() {
        if (this == UNINITIALIZED_VALUE) {
            return ".";
        } else if (this == RETURNADDRESS_VALUE) {
            return "A";
        } else if (this.getType().getSort() == Type.OBJECT) {
            return "R";
        } else {
            return type.getDescriptor();
        }
    }

    public static TypedValue referenceValue(String type) {
        return new TypedValue(Type.getObjectType(type));
    }

    public static TypedValue referenceValue(Type type) {
        return new TypedValue(type);
    }
}