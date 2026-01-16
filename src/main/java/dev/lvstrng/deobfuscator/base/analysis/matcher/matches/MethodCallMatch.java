package dev.lvstrng.deobfuscator.base.analysis.matcher.matches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.function.Predicate;

public class MethodCallMatch extends Match {
    private String owner, name, desc;
    private int opcode;
    private MethodCallMatch(int opcode, Predicate<AbstractInsnNode> predicate) {
        super(predicate);
        this.opcode = opcode;
    }

    public static MethodCallMatch of(int opcode) {
        return new MethodCallMatch(opcode, e -> e.getOpcode() == opcode);
    }

    public static MethodCallMatch ofStatic() {
        return of(Opcodes.INVOKESTATIC);
    }

    public static MethodCallMatch ofVirtual() {
        return of(Opcodes.INVOKEVIRTUAL);
    }

    public static MethodCallMatch ofSpecial() {
        return of(Opcodes.INVOKESPECIAL);
    }

    public static MethodCallMatch ofInterface() {
        return of(Opcodes.INVOKEINTERFACE);
    }

    public MethodCallMatch owner(String owner) {
        this.owner = owner;
        return this;
    }

    public MethodCallMatch name(String name) {
        this.name = name;
        return this;
    }

    public MethodCallMatch desc(String desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public boolean test(AbstractInsnNode insn) {
        if(!(insn instanceof MethodInsnNode call))
            return false;

        if(opcode != -1 && !super.test(insn)) //opcode
            return false;

        // names/desc
        return call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(desc);
    }
}
