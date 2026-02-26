package dev.lvstrng.deobfuscator.base.analysis.flow;

import dev.lvstrng.deobfuscator.base.analysis.interpreter.TypedValue;
import dev.lvstrng.deobfuscator.base.util.NamedOpcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private final ControlFlowGraph graph;
    public int blockIndex;

    public LabelNode label;
    public Block defaultBlock;
    public List<Block> successors = new ArrayList<>();
    public List<Block> predecessors = new ArrayList<>();

    public AbstractInsnNode edgeCause;
    public List<AbstractInsnNode> insns = new ArrayList<>();
    public Frame<TypedValue> startFrame, endFrame;

    public List<TryCatchBlockNode> traps = new ArrayList<>(), trapEnds = new ArrayList<>(), trapHandlers = new ArrayList<>();

    public Block(ControlFlowGraph graph, LabelNode lbl) {
        this.graph = graph;
        this.label = lbl;
    }

    public String getBlockContent() {
        var sb = new StringBuilder();
        sb.append("------------------\n");
        sb.append("|      ").append(this).append("\n");

        if(!traps.isEmpty()) {
            sb.append("| In Trap:\n");

            for (var trap : traps) {
                var start = graph.get(trap.start);
                var end = graph.get(trap.end);
                var handler = graph.get(trap.handler);

                sb.append("|\n\t").append(trap.type)
                        .append(" | [").append(start).append(" - ").append(end).append("]")
                        .append(" | Handler: ").append(handler);
            }
        }

        if(!trapEnds.isEmpty()) {
            sb.append("| In Trap End:\n");

            for (var trap : traps) {
                var start = graph.get(trap.start);
                var end = graph.get(trap.end);
                var handler = graph.get(trap.handler);

                sb.append("|\n\t").append(trap.type)
                        .append(" | [").append(start).append(" - ").append(end).append("]")
                        .append(" | Handler: ").append(handler);
            }
        }

        if(!trapHandlers.isEmpty()) {
            sb.append("| In Trap Handler:\n");

            for (var trap : traps) {
                var start = graph.get(trap.start);
                var end = graph.get(trap.end);

                sb.append("|\n\t").append(trap.type).append(" Handler: ")
                        .append(" | [").append(start).append(" - ").append(end).append("]");
            }
        }
        sb.append("|\n------------------\n");

        for(var insn : insns) {
            sb.append("\n\t").append(NamedOpcodes.map(insn.getOpcode()));
        }

        sb.append("\n[");
        for(var successor : successors) {
            sb.append("[").append(successor).append("] ");
        }
        sb.append("]");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "block" + blockIndex;
    }
}
