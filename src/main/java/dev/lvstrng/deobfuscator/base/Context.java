package dev.lvstrng.deobfuscator.base;

import dev.lvstrng.deobfuscator.base.transform.Transformer;
import dev.lvstrng.deobfuscator.base.tree.ClassWrapper;
import dev.lvstrng.deobfuscator.base.tree.HierarchyBuilder;
import dev.lvstrng.deobfuscator.base.util.ClassUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

@SuppressWarnings("all")
public class Context {
    private String in, out, libPath;
    private int writerFlags;

    private final Map<String, ClassWrapper> classMap = new HashMap<>();
    private final Map<String, ClassWrapper> libraries = new HashMap<>();
    private final Map<String, byte[]>       resources = new HashMap<>();

    private LibReader               libReader;
    private HierarchyBuilder hierarchyBuilder;
    private List<Transformer> transformers = new ArrayList<>();

    public void execute() {
        // ---------- INIT --------------
        this.libReader = new LibReader(this);
        this.hierarchyBuilder = new HierarchyBuilder(this);

        readInput();
        transform();
        writeOutput();
    }

    private void transform() {
        for(var transformer : transformers) {
            transformer.init(this);
            transformer.transform();
        }
    }

    private void readInput() {
        var inputFile = new File(in);
        if(!inputFile.exists())
            throw new IllegalArgumentException("Input file doesn't exist");

        libReader.loadLibs(libPath);
        try (var jar = new JarFile(inputFile)) {
            jar.stream().forEach(entry -> {
                try {
                    var name = entry.getName();
                    var stream = jar.getInputStream(entry);

                    if(name.endsWith(".class") || name.endsWith(".class/")) {
                        add(ClassUtils.readClass(stream));
                        return;
                    }

                    var bytes = stream.readAllBytes();
                    if(name.endsWith(".jar"))
                        libReader.parseJar(bytes);

                    resources.put(name, bytes);
                } catch (IOException _) {}
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        hierarchyBuilder.build();
    }

    private void writeOutput() {
        try (var jos = new JarOutputStream(new FileOutputStream(out))) {
            // -------- CLASSES -----------
            for(var name : classMap.keySet()) {
                var bytes = ClassUtils.writeClass(
                        classMap.get(name).core(), writerFlags
                );

                jos.putNextEntry(new ZipEntry(name + ".class"));
                jos.write(bytes);
                jos.closeEntry();
            }

            // -------- RESOURCES -----------
            for(var name : resources.keySet()) {
                jos.putNextEntry(new ZipEntry(name));
                jos.write(resources.get(name));
                jos.closeEntry();
            }

            jos.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ============== MISC =================

    public ClassWrapper forName(String name) {
        var clazz = classMap.get(name);
        if(clazz == null)
            clazz = libraries.get(name);

        return clazz;
    }

    public List<ClassWrapper> classes() {
        return new ArrayList<>(classMap.values());
    }

    public void add(ClassNode clazz) {
        classMap.put(clazz.name, new ClassWrapper(clazz, false));
    }

    public void addLib(ClassNode clazz) {
        libraries.put(clazz.name, new ClassWrapper(clazz, true));
    }

    // ============ CREATION ===============

    public static Context of() {
        return new Context();
    }

    public Context transformers(Transformer... transformers) {
        this.transformers.addAll(Arrays.asList(transformers));
        return this;
    }

    public Context input(String path) {
        this.in = path;
        return this;
    }

    public Context output(String path) {
        this.out = path;
        return this;
    }

    public Context libs(String path) {
        this.libPath = path;
        return this;
    }

    public Context writerFlags(int flags) {
        this.writerFlags = flags;
        return this;
    }
}
