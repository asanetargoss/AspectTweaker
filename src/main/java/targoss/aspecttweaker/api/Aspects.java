package targoss.aspecttweaker.api;

import java.util.LinkedHashSet;
import java.util.Map;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import thaumcraft.api.aspects.AspectList;

@ZenClass("aspecttweaker.Aspects")
public class Aspects {
	@ZenMethod
	public static void setAspects(IIngredient ingredient, IAspect[] aspects) {
		//TODO: Add oredict support
		if (!(ingredient instanceof IItemStack)) {
			throw new IllegalArgumentException("The aspect must be an instance of IIngredient");
		}
		MineTweakerAPI.apply(new ChangeItemAspectsAction((IItemStack)ingredient, aspects));
	}
	
	@ZenMethod
	public static void removeAspects(IIngredient ingredient) {
		//TODO: Add oredict support
		if (!(ingredient instanceof IItemStack)) {
			throw new IllegalArgumentException("The aspect must be an instance of IIngredient");
		}
		MineTweakerAPI.apply(new ChangeItemAspectsAction((IItemStack)ingredient, new IAspect[0]));
	}
	
	public static void clearAppliedActions() {
		ChangeItemAspectsAction.appliedItemActions.clear();
	}
	
	public static void applyToObjectTags(Map<String, AspectList> objectTags) {
		for (ChangeItemAspectsAction action : ChangeItemAspectsAction.appliedItemActions) {
			if (action.aspects != null) {
				objectTags.put(action.aspectKey, action.aspects);
			}
			else {
				objectTags.remove(action.aspectKey);
			}
		}
    	
	}
	
	public static class ChangeItemAspectsAction implements IUndoableAction {
		protected static final LinkedHashSet<ChangeItemAspectsAction> appliedItemActions = new LinkedHashSet<ChangeItemAspectsAction>();
		
		protected final String itemName;
		protected final String aspectKey;
		protected final AspectList aspects;
		
		public ChangeItemAspectsAction(IItemStack iItemStack, IAspect[] aspects) {
			ItemStack itemStack = MineTweakerMC.getItemStack(iItemStack).copy();
			itemStack.stackSize = 1;
			this.itemName = itemStack.getDisplayName();
			this.aspectKey = itemStack.serializeNBT().toString();
			this.aspects = new AspectList();
			for (int i = 0; i < aspects.length; i++) {
				this.aspects.add(aspects[i].getAspect(), aspects[i].getAmount());
			}
		}
		
		@Override
		public void apply() {
			appliedItemActions.add(this);
		}

		@Override
		public void undo() {
			appliedItemActions.remove(this);
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public String describe() {
			return "Changing aspects for " + this.itemName;
		}

		@Override
		public String describeUndo() {
			return "Reverting aspects for " + this.itemName;
		}

		@Override
		public Object getOverrideKey() {
			return null;
		}
	}
}
