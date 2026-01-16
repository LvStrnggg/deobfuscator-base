package dev.lvstrng.deobfuscator.base.tree;

import dev.lvstrng.deobfuscator.base.Context;

import java.util.HashSet;
import java.util.Set;

// TODO: Full graph instead of just parent classes
public class HierarchyBuilder {
    private final Context context;
    private final Set<ClassWrapper> built = new HashSet<>();
    private final Set<String> missing = new HashSet<>();

    public HierarchyBuilder(Context context) {
        this.context = context;
    }

    public void build() {
        for (var clazz : context.classes()) {
            buildDeep(clazz);
        }
    }

    private void buildDeep(ClassWrapper clazz) {
        if (!built.add(clazz))
            return;

        var parents = new HashSet<ClassWrapper>();
        collect(clazz, parents);

        for (var parent : parents) {
            clazz.parents().add(parent);
            parent.children().add(clazz);
        }
    }

    private void collect(ClassWrapper clazz, Set<ClassWrapper> out) {
        if (clazz.superName() != null) {
            var superClass = context.forName(clazz.superName());
            if(superClass == null) {
                if(missing.add(clazz.superName()))
                    System.out.println("Missing `" + clazz.superName() + "`, exported JAR can be faulty. Install correct libraries if you wish");

                return;
            }

            if (out.add(superClass)) {
                collect(superClass, out);
            }
        }

        if (clazz.interfaces() == null)
            return;

        for (var itf : clazz.interfaces()) {
            var itfClass = context.forName(itf);
            if(itfClass == null) {
                if(missing.add(itf))
                    System.out.println("Missing `" + itf + "`, exported JAR can be faulty. Install correct libraries if you wish");
                return;
            }

            if (out.add(itfClass))
                collect(itfClass, out);
        }
    }
}
