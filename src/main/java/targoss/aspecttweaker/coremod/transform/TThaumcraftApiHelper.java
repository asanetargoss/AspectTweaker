package targoss.aspecttweaker.coremod.transform;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import targoss.aspecttweaker.coremod.MethodPatcher;

public class TThaumcraftApiHelper extends MethodPatcher {
    protected static final String THAUMCRAFT_API_HELPER = "thaumcraft.api.ThaumcraftApiHelper";
    protected static final String ARE_ITEM_STACKS_EQUAL_FOR_CRAFTING = "areItemStacksEqualForCrafting";
    protected static final String ITEM_MATCHES = "itemMatches";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.contentEquals(THAUMCRAFT_API_HELPER)) {
            return transformClass(transformedName, basicClass, 0);
        }
        return basicClass;
    }

    @Override
    public void transformMethod(MethodNode method) {
        if (method.name.equals(ARE_ITEM_STACKS_EQUAL_FOR_CRAFTING)) {
            InsnList insns = method.instructions;
            ListIterator<AbstractInsnNode> iter = insns.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.INVOKESTATIC && ((MethodInsnNode)insn).name.equals(ITEM_MATCHES)) {
                    InsnList patch = new InsnList();
                    // Before: return OreDictionary.itemMatches((ItemStack)in, stack0, false);
                    // After: return OreDictionary.itemMatches(stack0, (ItemStack)in, false);
                    // This fixes a bug when crafting items with item meta wildcards (ex: using coal/charcoal to craft alumentum)
                    patch.add(new InsnNode(Opcodes.POP));
                    patch.add(new InsnNode(Opcodes.SWAP));
                    patch.add(new InsnNode(Opcodes.ICONST_0));
                    insns.insertBefore(insn, patch);
                }
            }
        }
    }

}
