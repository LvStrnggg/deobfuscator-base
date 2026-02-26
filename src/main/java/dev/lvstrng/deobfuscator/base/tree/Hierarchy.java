package dev.lvstrng.deobfuscator.base.tree;

import dev.lvstrng.deobfuscator.base.Context;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

// TODO: Full graph instead of just parent classes
public class Hierarchy {
    private final Context context;
    private final Set<ClassWrapper> built = new HashSet<>();

    public Hierarchy(Context context) {
        this.context = context;
    }

    public void build() {
        for (var clazz : context.classes()) {
            buildClassHierarchy(clazz);
        }
    }

    private void buildClassHierarchy(ClassWrapper clazz) {
        if (!built.add(clazz))
            return;

        var parents = new LinkedHashSet<ClassWrapper>();
        collectClasses(clazz, parents);

        for (var parent : parents.stream().filter(e -> !e.equals(clazz)).toList()) {
            clazz.parents().add(parent);
            parent.children().add(clazz);
        }
    }

    private void collectClasses(ClassWrapper clazz, Set<ClassWrapper> out) {
        if(!out.add(clazz))
            return;

        if(clazz.superName() != null) {
            var superClass = context.forName(clazz.superName());
            collectClasses(superClass, out);
        }

        if(clazz.interfaces() != null) {
            for(var itf : clazz.interfaces()) {
                var itfClass = context.forName(itf);
                collectClasses(itfClass, out);
            }
        }
    }
}
