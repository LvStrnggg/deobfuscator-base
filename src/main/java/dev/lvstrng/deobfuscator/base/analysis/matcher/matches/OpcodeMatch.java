package dev.lvstrng.deobfuscator.base.analysis.matcher.matches;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.function.Predicate;

public class OpcodeMatch extends Match {
    private OpcodeMatch(int opcode, Predicate<AbstractInsnNode> predicate) {
        super(e -> e.getOpcode() == opcode && predicate.test(e));
    }

    public static OpcodeMatch of(int opcode) {
        return new OpcodeMatch(opcode, _ -> true);
    }

    public static OpcodeMatch of(int opcode, Predicate<AbstractInsnNode> pred) {
        return new OpcodeMatch(opcode, pred);
    }
}
