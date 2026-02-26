package dev.lvstrng.deobfuscator.base.tree;

import dev.lvstrng.deobfuscator.base.analysis.interpreter.TypedInterpreter;
import dev.lvstrng.deobfuscator.base.analysis.interpreter.TypedValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.lang.reflect.Modifier;
import java.util.*;

public class ClassWrapper {
    private ClassNode core;
    private boolean library;
    private Set<ClassWrapper> parents;
    private Set<ClassWrapper> children;

    public ClassWrapper(ClassNode core, boolean lib) {
        this.core(core, lib);
    }

    public Map<AbstractInsnNode, Frame<TypedValue>> frames(MethodNode method) {
        try {
            var frames = new HashMap<AbstractInsnNode, Frame<TypedValue>>();
            var frameArr = new Analyzer<>(new TypedInterpreter()).analyzeAndComputeMaxs(name(), method);

            for(int i = 0; i < method.instructions.size(); i++) {
                var insn = method.instructions.get(i);
                var frame = frameArr[i];

                frames.put(insn, frame);
            }

            return frames;
        } catch (AnalyzerException _) {}

        return null;
    }

    public Optional<MethodNode> findMethod(String name, String desc) {
        return methods().stream()
                .filter(e -> e.name.equals(name))
                .filter(e -> e.desc.equals(desc))
                .findFirst();
    }

    public MethodNode findOrCreateClinit() {
        var clinit = findMethod("<clinit>", "()V").orElse(null);
        if(clinit == null) {
            clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            methods().add(clinit);
        }

        return clinit;
    }

    public Optional<MethodNode> clinit() {
        return findMethod("<clinit>", "()V");
    }

    // ============= BASIC GETTERS/SETTERS ===============

    public boolean isInterface() {
        return Modifier.isInterface(access());
    }

    public boolean library() {
        return library;
    }

    public Set<ClassWrapper> parents() {
        return parents;
    }

    public Set<ClassWrapper> children() {
        return children;
    }

    public List<MethodNode> methods() {
        return core.methods;
    }

    public List<FieldNode> fields() {
        return core.fields;
    }

    public String name() {
        return core.name;
    }

    public String superName() {
        return core.superName;
    }

    public int access() {
        return core.access;
    }

    public List<String> interfaces() {
        return core.interfaces;
    }

    public ClassNode core() {
        return core;
    }

    public void core(ClassNode core, boolean lib) {
        this.core = core;
        this.library = lib;

        this.children = new HashSet<>();
        this.parents = new HashSet<>();
    }
}
