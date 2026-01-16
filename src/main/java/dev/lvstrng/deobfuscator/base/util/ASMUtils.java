package dev.lvstrng.deobfuscator.base.util;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ASMUtils implements Opcodes {
    public static AbstractInsnNode pushString(String value) {
        return new LdcInsnNode(value);
    }

    public static AbstractInsnNode pushFloat(float value) {
        if (value == 0.0f || value == 1.0f || value == 2.0f) {
            return new InsnNode(FCONST_0 + (int) value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    public static AbstractInsnNode pushDouble(double value) {
        if (value == 0.0 || value == 1.0) {
            return new InsnNode(DCONST_0 + (int) value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    public static AbstractInsnNode pushInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(SIPUSH, value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    public static AbstractInsnNode pushLong(long value) {
        if (value == 0 || value == 1) {
            return new InsnNode(LCONST_0 + (int) value);
        } else {
            return new LdcInsnNode(value);
        }
    }

    public static boolean isIntPush(AbstractInsnNode node) {
        if(node.getOpcode() >= ICONST_M1 && node.getOpcode() <= ICONST_5)
            return true;

        if(node instanceof IntInsnNode n && n.getOpcode() != NEWARRAY)
            return true;

        return node instanceof LdcInsnNode ldc && ldc.cst instanceof Integer;
    }

    public static boolean isLongPush(AbstractInsnNode node) {
        if(node.getOpcode() == LCONST_0 || node.getOpcode() == LCONST_1)
            return true;

        return node instanceof LdcInsnNode ldc && ldc.cst instanceof Long;
    }

    public static int getInt(AbstractInsnNode node) {
        if(node.getOpcode() >= ICONST_M1 && node.getOpcode() <= ICONST_5)
            return node.getOpcode() - ICONST_0;

        if(node instanceof IntInsnNode n && n.getOpcode() != NEWARRAY)
            return n.operand;

        return (Integer) ((LdcInsnNode) node).cst;
    }

    public static long getLong(AbstractInsnNode node) {
        if(node.getOpcode() == LCONST_0 || node.getOpcode() == LCONST_1)
            return node.getOpcode() - LCONST_0;

        return (long) ((LdcInsnNode) node).cst;
    }

    public static List<AbstractInsnNode> getInstructionsBetween(AbstractInsnNode start, AbstractInsnNode end) {
        return getInstructionsBetween(start, end, true, true);
    }

    public static List<AbstractInsnNode> getInstructionsBetween(AbstractInsnNode start, AbstractInsnNode end, boolean includeStart, boolean includeEnd) {
        var instructions = new ArrayList<AbstractInsnNode>();
        if (includeStart)
            instructions.add(start);

        while ((start = start.getNext()) != null && start != end) {
            instructions.add(start);
        }

        if (includeEnd)
            instructions.add(end);
        return instructions;
    }

    public static boolean isLoad(AbstractInsnNode v) {
        return v.getOpcode() >= ILOAD && v.getOpcode() <= ALOAD;
    }

    public static boolean isStore(AbstractInsnNode v) {
        return v.getOpcode() >= ISTORE && v.getOpcode() <= ASTORE;
    }

    public static boolean isArrayLoad(int opcode) {
        return opcode >= IALOAD && opcode <= SALOAD;
    }

    public static boolean isArrayStore(int opcode) {
        return opcode >= IASTORE && opcode <= SASTORE;
    }
}
