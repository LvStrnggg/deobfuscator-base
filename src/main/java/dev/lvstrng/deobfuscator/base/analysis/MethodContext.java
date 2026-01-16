package dev.lvstrng.deobfuscator.base.analysis;

import dev.lvstrng.deobfuscator.base.tree.ClassWrapper;
import org.objectweb.asm.tree.MethodNode;

public class MethodContext {
    private final ClassWrapper owner;
    private final MethodNode method;

    private MethodContext(ClassWrapper owner, MethodNode method) {
        this.owner = owner;
        this.method = method;
    }

    public static MethodContext of(ClassWrapper owner, MethodNode method) {
        return new MethodContext(owner, method);
    }

    public MethodNode method() {
        return method;
    }

    public ClassWrapper owner() {
        return owner;
    }
}
