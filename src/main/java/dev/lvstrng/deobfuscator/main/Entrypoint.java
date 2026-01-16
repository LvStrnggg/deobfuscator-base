package dev.lvstrng.deobfuscator.main;

import dev.lvstrng.deobfuscator.base.Context;
import org.objectweb.asm.ClassWriter;

public class Entrypoint {
    public static void main(String[] args) {
        Context.of()
            .input("zkm.jar")
            .output("out.jar")
            .transformers( // add your transformers here
            )
            //.libs("libs/")
            .writerFlags(ClassWriter.COMPUTE_MAXS)
            .execute();
    }
}
