package dev.lvstrng.deobfuscator.base.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

public class ClassUtils {
    public static ClassNode readClass(byte[] bytes) {
        var node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public static byte[] writeClass(ClassNode classNode, int writerFlags) {
        var writer = new ClassWriter(writerFlags);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static ClassNode readClass(InputStream stream) throws IOException {
        return readClass(stream.readAllBytes());
    }

    public static ClassNode readAsDependency(InputStream in) throws IOException {
        var node = new ClassNode();
        new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        return node;
    }
}
