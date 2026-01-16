package dev.lvstrng.deobfuscator.base.analysis.matcher.matches;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Match {
    private final Predicate<AbstractInsnNode> predicate;
    private String capture = null;

    public Match(Predicate<AbstractInsnNode> predicate) {
        this.predicate = predicate;
    }

    public boolean test(AbstractInsnNode insn) {
        return predicate.test(insn);
    }

    public Match capture(String name) {
        this.capture = name;
        return this;
    }

    public String capture() {
        return capture;
    }
}
