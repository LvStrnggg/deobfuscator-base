package dev.lvstrng.deobfuscator.base.analysis.flow;

import dev.lvstrng.deobfuscator.base.tree.ClassWrapper;
import dev.lvstrng.deobfuscator.base.util.ASMUtils;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ControlFlowGraph {
    private final ClassWrapper clazz;
    private final MethodNode method;
    private final List<Block> blocks;
    private final Map<LabelNode, Block> labelToBlock;
    private final Map<JumpInsnNode, LabelNode> flowsInto;

    private Block firstBlock, lastBlock;

    public ControlFlowGraph(ClassWrapper clazz, MethodNode method) {
        this.clazz = clazz;
        this.method = method;

        this.blocks = new ArrayList<>();
        this.labelToBlock = new HashMap<>();
        this.flowsInto = new HashMap<>();
    }

    public boolean build() {
        if(method.instructions.size() == 0)
            return false;

        var frames = clazz.frames(method);
        if(frames == null)
            return false;

        this.prepare();

        Block current = null;
        for(var insn : method.instructions) {
            lastBlock = current;
            if(firstBlock == null)
                firstBlock = current;

            if(insn instanceof LabelNode lbl) {
                var labelBlock = get(lbl);
                if(current != null) {
                    //simple fall-through code
                    if(current.defaultBlock == null) {
                        current.defaultBlock = labelBlock;
                        vertex(current, labelBlock);
                    }

                    //carry all existing traps onto next block
                    labelBlock.traps.addAll(current.traps);
                    labelBlock.trapEnds.addAll(current.trapEnds);
                    labelBlock.trapHandlers.addAll(current.trapHandlers);

                    for(var trap : method.tryCatchBlocks) {
                        if(trap.end == lbl)
                            labelBlock.traps.remove(trap);

                        // if handler and end aren't equal and current block is a handler, remove from end
                        if(trap.handler == lbl && trap.end != lbl)
                            labelBlock.trapEnds.remove(trap);

                        //where an end block flows into a handler, the handler ends there
                        if(labelBlock.predecessors.stream().anyMatch(e -> e.trapEnds.contains(trap)))
                            labelBlock.trapHandlers.remove(trap);
                    }

                    //handle true last frame
                    // last block insn
                    //        |
                    //       V
                    // insn after last block insn
                    current.endFrame = frames.get(current.insns.getLast().getNext());
                }

                current = labelBlock;
            }

            // cant continue block logic if block is null
            if(current == null)
                continue;

            current.insns.add(insn);

            //handle first frame
            if(insn != current.label && current.startFrame == null) {
                current.startFrame = frames.get(insn);
            }

            // handle edges
            switch (insn) {
                case JumpInsnNode jmp -> {
                    current.edgeCause = jmp;

                    var target = get(jmp.label);
                    vertex(current, target);

                    current.defaultBlock = target;
                    if(jmp.getOpcode() == GOTO) {
                        current = null;
                        break;
                    }

                    var fallThru = get(flowsInto.get(jmp));
                    current.defaultBlock = fallThru;
                    vertex(current, fallThru);

                    current = null;
                }
                case TableSwitchInsnNode table -> {
                    current.edgeCause = table;

                    var defaultBlock = get(table.dflt);
                    vertex(current, defaultBlock);
                    current.defaultBlock = defaultBlock;

                    for(var route : table.labels) {
                        var block = get(route);
                        vertex(current, block);
                    }

                    current = null;
                }
                case LookupSwitchInsnNode lookup -> {
                    current.edgeCause = lookup;

                    var defaultBlock = get(lookup.dflt);
                    vertex(current, defaultBlock);
                    current.defaultBlock = defaultBlock;

                    for(var route : lookup.labels) {
                        var block = get(route);
                        vertex(current, block);
                    }

                    current = null;
                }
                case null -> {}
                default -> {
                    if(ASMUtils.isReturn(insn) || insn.getOpcode() == ATHROW) {
                        current.edgeCause = insn;
                        current = null;
                    }
                }
            }
        }

        return true;
    }

    private void prepare() {
        // we need this to build the first block
        if(!(method.instructions.getFirst() instanceof LabelNode))
            method.instructions.insert(new LabelNode());

        for(var insn : method.instructions) {
            if(!(insn instanceof JumpInsnNode jmp))
                continue;

            if(jmp.getOpcode() == GOTO) {
                flowsInto.put(jmp, jmp.label);
                continue;
            }

            if(jmp.getNext() instanceof LabelNode lbl) {
                flowsInto.put(jmp, lbl);
                continue;
            }

            // add new label which the jump will flow into/fallthrough
            var lbl = new LabelNode();
            method.instructions.insert(jmp, lbl);
            flowsInto.put(jmp, lbl);
        }

        int blockIndex = 0;
        for(var insn : method.instructions) {
            if(!(insn instanceof LabelNode lbl))
                continue;

            var block = new Block(this, lbl);
            block.blockIndex = blockIndex++;

            for(var trap : method.tryCatchBlocks) {
                if(trap.start == lbl)
                    block.traps.add(trap);

                if(trap.end == lbl)
                    block.trapEnds.add(trap);

                if(trap.handler == lbl)
                    block.trapHandlers.add(trap);
            }

            blocks.add(block);
            labelToBlock.put(lbl, block);
        }
    }

    private void vertex(Block src, Block dst) {
        src.successors.add(dst);
        dst.predecessors.add(src);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Block get(LabelNode lbl) {
        return labelToBlock.get(lbl);
    }

    public Block firstBlock() {
        return firstBlock;
    }

    public Block lastBlock() {
        return lastBlock;
    }
}
