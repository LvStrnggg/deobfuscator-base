package dev.lvstrng.deobfuscator.base.transform;

import dev.lvstrng.deobfuscator.base.Context;
import dev.lvstrng.deobfuscator.base.tree.ClassWrapper;
import org.objectweb.asm.Opcodes;

import java.util.List;

public abstract class Transformer implements Opcodes {
    private Context context;
    private int changes = 0;

    public void init(Context context) {
        this.context = context;
    }

    public abstract void transform();

    // ============= MISC =============

    public String name() {
        return this.getClass().getName();
    }

    public void markChange() {
        changes++;
    }

    public int changes() {
        return changes;
    }

    public Context context() {
        return context;
    }

    public List<ClassWrapper> classes() {
        return context.classes();
    }
}
