package dev.lvstrng.deobfuscator.base.analysis.matcher;

import dev.lvstrng.deobfuscator.base.analysis.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MatchContext {
    private final MethodContext method;
    private final Set<AbstractInsnNode> insns;
    private final Map<String, AbstractInsnNode> captures;

    protected MatchContext(MethodContext method) {
        this.method = method;
        this.insns = new HashSet<>();
        this.captures = new HashMap<>();
    }

    public void add(AbstractInsnNode insn) {
        insns.add(insn);
    }

    public MethodContext method() {
        return method;
    }

    public Set<AbstractInsnNode> insns() {
        return insns;
    }

    public Map<String, AbstractInsnNode> captures() {
        return captures;
    }

    public AbstractInsnNode get(String s) {
        return captures.get(s);
    }

    public void put(String s, AbstractInsnNode node) {
        captures.put(s, node);
    }

    public void removeAll() {
        insns.forEach(method.method().instructions::remove);
    }
}
