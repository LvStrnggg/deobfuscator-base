package dev.lvstrng.deobfuscator.base.analysis.interpreter;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

public class TypedInterpreter extends Interpreter<TypedValue> implements Opcodes {
    public static final Type NULL_TYPE = Type.getObjectType("null");

    public TypedInterpreter() {
        super(/* latest api = */ ASM9);
        if (getClass() != TypedInterpreter.class) {
            throw new IllegalStateException();
        }
    }

    protected TypedInterpreter(final int api) {
        super(api);
    }

    @Override
    public TypedValue newValue(final Type type) {
        if (type == null) {
            return TypedValue.UNINITIALIZED_VALUE;
        }
        return switch (type.getSort()) {
            case Type.VOID -> null;
            case Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> TypedValue.INT_VALUE;
            case Type.FLOAT -> TypedValue.FLOAT_VALUE;
            case Type.LONG -> TypedValue.LONG_VALUE;
            case Type.DOUBLE -> TypedValue.DOUBLE_VALUE;
            case Type.ARRAY, Type.OBJECT -> TypedValue.referenceValue(type);
            default -> throw new AssertionError();
        };
    }

    @Override
    public TypedValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return newValue(NULL_TYPE);
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return TypedValue.INT_VALUE;
            case LCONST_0:
            case LCONST_1:
                return TypedValue.LONG_VALUE;
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return TypedValue.FLOAT_VALUE;
            case DCONST_0:
            case DCONST_1:
                return TypedValue.DOUBLE_VALUE;
            case BIPUSH:
            case SIPUSH:
                return TypedValue.INT_VALUE;
            case LDC:
                Object value = ((LdcInsnNode) insn).cst;
                switch (value) {
                    case Integer i -> {
                        return TypedValue.INT_VALUE;
                    }
                    case Float v -> {
                        return TypedValue.FLOAT_VALUE;
                    }
                    case Long l -> {
                        return TypedValue.LONG_VALUE;
                    }
                    case Double v -> {
                        return TypedValue.DOUBLE_VALUE;
                    }
                    case String s -> {
                        return newValue(Type.getObjectType("java/lang/String"));
                    }
                    case Type type -> {
                        int sort = type.getSort();

                        if (sort == Type.OBJECT || sort == Type.ARRAY) {
                            return newValue(Type.getObjectType("java/lang/Class"));
                        } else if (sort == Type.METHOD) {
                            return newValue(Type.getObjectType("java/lang/invoke/MethodType"));
                        } else {
                            throw new AnalyzerException(insn, "Illegal LDC value " + value);
                        }
                    }
                    case Handle handle -> {
                        return newValue(Type.getObjectType("java/lang/invoke/MethodHandle"));
                    }
                    case ConstantDynamic condy -> {
                        return newValue(Type.getType(condy.getDescriptor()));
                    }
                    case null, default -> throw new AnalyzerException(insn, "Illegal LDC value " + value);
                }
            case JSR:
                return TypedValue.RETURNADDRESS_VALUE;
            case GETSTATIC:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEW:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            default:
                throw new AssertionError();
        }
    }

    @Override
    public TypedValue copyOperation(AbstractInsnNode insn, TypedValue value) throws AnalyzerException {
        return value;
    }

    @Override
    public TypedValue unaryOperation(final AbstractInsnNode insn, final TypedValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case INEG:
            case IINC:
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return TypedValue.INT_VALUE;
            case FNEG:
            case I2F:
            case L2F:
            case D2F:
                return TypedValue.FLOAT_VALUE;
            case LNEG:
            case I2L:
            case F2L:
            case D2L:
                return TypedValue.LONG_VALUE;
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                return TypedValue.DOUBLE_VALUE;
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case PUTSTATIC:
                return null;
            case GETFIELD:
                return newValue(Type.getType(((FieldInsnNode) insn).desc));
            case NEWARRAY:
                return switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN -> newValue(Type.getType("[Z"));
                    case T_CHAR -> newValue(Type.getType("[C"));
                    case T_BYTE -> newValue(Type.getType("[B"));
                    case T_SHORT -> newValue(Type.getType("[S"));
                    case T_INT -> newValue(Type.getType("[I"));
                    case T_FLOAT -> newValue(Type.getType("[F"));
                    case T_DOUBLE -> newValue(Type.getType("[D"));
                    case T_LONG -> newValue(Type.getType("[J"));
                    default -> throw new AnalyzerException(insn, "Invalid array type");
                };
            case ANEWARRAY:
                return newValue(Type.getType("[" + Type.getObjectType(((TypeInsnNode) insn).desc)));
            case ARRAYLENGTH:
                return TypedValue.INT_VALUE;
            case ATHROW:
                return null;
            case CHECKCAST:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            case INSTANCEOF:
                return TypedValue.INT_VALUE;
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public TypedValue binaryOperation(final AbstractInsnNode insn, final TypedValue value1, final TypedValue value2) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return TypedValue.INT_VALUE;
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                return TypedValue.FLOAT_VALUE;
            case LALOAD:
            case LADD:
            case LSUB:
            case LMUL:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return TypedValue.LONG_VALUE;
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                return TypedValue.DOUBLE_VALUE;
            case AALOAD: {
                var arrayType = value1.getType();
                if(!arrayType.getDescriptor().startsWith("["))
                    throw new AnalyzerException(insn, "Illegal object array load, array type: " + arrayType.getDescriptor());

                return newValue(arrayType.getElementType());
            }
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return TypedValue.INT_VALUE;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case PUTFIELD:
                return null;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public TypedValue ternaryOperation(
            final AbstractInsnNode insn,
            final TypedValue value1,
            final TypedValue value2,
            final TypedValue value3) {
        return null;
    }

    public TypedValue naryOperation(AbstractInsnNode insn, List<? extends TypedValue> values) {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC) {
            return newValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, TypedValue value, TypedValue expected) {
        // Nothing to do.
    }

    @Override
    public TypedValue merge(final TypedValue value1, final TypedValue value2) {
        if (!value1.equals(value2))
            return TypedValue.UNINITIALIZED_VALUE;

        return value1;
    }
}
