package targoss.aspecttweaker.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.annotation.Nullable;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.recipes.ShapedRecipe;
import minetweaker.api.recipes.ShapelessRecipe;
import minetweaker.mc1102.recipes.RecipeConverter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("aspecttweaker.RecipeExamples")
public class RecipeExamples {
    @ZenMethod
    public static void removeAllForId(String recipeOutputId) {
        MineTweakerAPI.apply(new RemoveRecipeExampleAction(recipeOutputId));
    }
    
    @ZenMethod
    public static void removeForIdMatchingItem(String recipeOutputId, IItemStack recipeOutput) {
        MineTweakerAPI.apply(new RemoveSpecificRecipeExampleAction(recipeOutputId, recipeOutput));
    }

    @ZenMethod
    public static void addShaped(IItemStack recipeOutput, IIngredient[][] ingredients, String customName) {
        MineTweakerAPI.apply(new AddShapedRecipeExampleAction((IItemStack)recipeOutput, ingredients, customName));
    }

    @ZenMethod
    public static void addShapeless(IItemStack recipeOutput, IIngredient[] ingredients, String customName) {
        MineTweakerAPI.apply(new AddShapelessRecipeExampleAction((IItemStack)recipeOutput, ingredients, customName));
    }
    
    public static void clearAppliedActions() {
        RecipeExampleAction.appliedRecipeExampleActions.clear();
    }
    
    public static void applyToCraftingRecipeCatalog(Map<String, Object> craftingRecipeCatalog) {
        for (RecipeExampleAction action : RecipeExampleAction.appliedRecipeExampleActions) {
            action.doAction(craftingRecipeCatalog);
        }
    }
    
    public static abstract class RecipeExampleAction implements IUndoableAction {
        public static final LinkedHashSet<RecipeExampleAction> appliedRecipeExampleActions = new LinkedHashSet<RecipeExampleAction>();
        
        public abstract void doAction(Map<String, Object> recipeCatalog);

        @Override
        public void apply() {
            appliedRecipeExampleActions.add(this);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            appliedRecipeExampleActions.remove(this);
        }

        @Override
        public Object getOverrideKey() {
            return null;
        }
    }
    
    public static class RemoveRecipeExampleAction extends RecipeExampleAction {
        protected final String recipeOutputName;
        protected final String recipeOutputId;
        
        public RemoveRecipeExampleAction(String recipeOutputId) {
            this.recipeOutputName = this.recipeOutputId = recipeOutputId;
        }
        
        public RemoveRecipeExampleAction(IItemStack recipeOutput) {
            this.recipeOutputName = recipeOutput.getDisplayName();
            this.recipeOutputId = recipeOutput.getDefinition().getId();
        }
        
        @Override
        public void doAction(Map<String, Object> recipeCatalog) {
            recipeCatalog.remove(recipeOutputId);
        }

        @Override
        public String describe() {
            return "Removing recipe example for " + recipeOutputName;
        }

        @Override
        public String describeUndo() {
            return "Reverting removing recipe example for " + recipeOutputName;
        }
    }
    
    protected static boolean stackEmpty(@Nullable ItemStack stack) {
        return stack == null;
    }
    
    public static class RemoveSpecificRecipeExampleAction extends RecipeExampleAction {
        protected final String recipeOutputName;
        protected final String recipeOutputId;
        protected final IItemStack recipeOutput;
        
        public RemoveSpecificRecipeExampleAction(String recipeOutputId, IItemStack recipeOutput) {
            this.recipeOutputName = recipeOutput.getDisplayName();
            this.recipeOutputId = recipeOutputId;
            this.recipeOutput = recipeOutput;
        }
        
        @Override
        public void doAction(Map<String, Object> recipeCatalog) {
            Object possiblyObjs = recipeCatalog.get(recipeOutputId);
            if (possiblyObjs instanceof Object[]) {
                Object[] objs = (Object[])possiblyObjs;
                ArrayList<IRecipe> recipesToKeep = new ArrayList<IRecipe>(objs.length - 1);
                for (Object obj : objs) {
                    if (!(obj instanceof IRecipe)) {
                        continue;
                    }
                    IRecipe oldRecipe = (IRecipe)obj;
                    ItemStack result = oldRecipe.getRecipeOutput();
                    if (stackEmpty(result)) {
                        continue;
                    }
                    if (result.getItem().getRegistryName().toString().equals(recipeOutput.getDefinition().getId()) &&
                            result.getItemDamage() == recipeOutput.getDamage()) {
                        continue;
                    }

                    recipesToKeep.add(oldRecipe);
                }
                if (recipesToKeep.size() > 0) {
                    IRecipe[] irecipesToKeep = new IRecipe[recipesToKeep.size()];
                    for (int i = 0; i < recipesToKeep.size(); ++i) {
                        irecipesToKeep[i] = recipesToKeep.get(i);
                    }
                    recipeCatalog.put(recipeOutputId, irecipesToKeep);
                }
                else {
                    recipeCatalog.remove(recipeOutputId);
                }
            }
        }

        @Override
        public String describe() {
            return "Removing recipe example for " + recipeOutputName + " in recipe list " + recipeOutputId;
        }

        @Override
        public String describeUndo() {
            return "Reverting removing recipe example for " + recipeOutputName + " in recipe list " + recipeOutputId;
        }
    }
    
    protected static <T> T[] getCombinedArray(T[] t1, T t2) {
        T[] ts = Arrays.copyOf(t1, t1.length + 1);
        ts[t1.length] = t2;
        return ts;
    }
    
    protected static Object combineRecipes(@Nullable Object r1, IRecipe r2) {
        if (r1 instanceof IRecipe[]) {
            return getCombinedArray((IRecipe[])r1, r2);
        }
        else {
            return new IRecipe[] { r2 };
        }
    }
    
    public static class AddShapedRecipeExampleAction extends RecipeExampleAction {
        protected final String recipeOutputName;
        protected final String recipeOutputId;
        protected final IRecipe recipe;
        
        public AddShapedRecipeExampleAction(IItemStack recipeOutput, IIngredient[][] ingredients, String customId) {
            recipeOutputName = recipeOutput.getDisplayName();
            recipeOutputId = customId;
            // This works because RecipeConverter.convert should output either a ShapedRecipes or a ShapedOreRecipe,
            // which Thaumcraft knows how to convert to a recipe display in the Thaumonomicon (see GuiResearchPage).
            ShapedRecipe tweakerRecipe = new ShapedRecipe(recipeOutput, ingredients, null, null, false);
            recipe = RecipeConverter.convert(tweakerRecipe);
        }
        
        @Override
        public void doAction(Map<String, Object> recipeCatalog) {
            Object oldRecipeOrRecipeList = recipeCatalog.get(recipeOutputId);
            Object newRecipeOrRecipeList = combineRecipes(oldRecipeOrRecipeList, recipe);
            recipeCatalog.put(recipeOutputId, newRecipeOrRecipeList);
        }

        @Override
        public String describe() {
            return "Adding shaped recipe example for " + recipeOutputName + " with id " + recipeOutputId;
        }

        @Override
        public String describeUndo() {
            return "Reverting adding shaped recipe example for " + recipeOutputName + " with id " + recipeOutputId;
        }
    }
    
    public static class AddShapelessRecipeExampleAction extends RecipeExampleAction {
        protected final String recipeOutputName;
        protected final String recipeOutputId;
        protected final IRecipe recipe;
        
        public AddShapelessRecipeExampleAction(IItemStack recipeOutput, IIngredient[] ingredients, @Nullable String customId) {
            recipeOutputName = recipeOutput.getDisplayName();
            recipeOutputId = customId;
            // This works because RecipeConverter.convert should output either a ShapelessRecipes or a ShapelessOreRecipe,
            // which Thaumcraft knows how to convert to a recipe display in the Thaumonomicon (see GuiResearchPage).
            ShapelessRecipe tweakerRecipe = new ShapelessRecipe(recipeOutput, ingredients, null, null);
            recipe = RecipeConverter.convert(tweakerRecipe);
        }
        
        @Override
        public void doAction(Map<String, Object> recipeCatalog) {
            Object oldRecipeOrRecipeList = recipeCatalog.get(recipeOutputId);
            Object newRecipeOrRecipeList = combineRecipes(oldRecipeOrRecipeList, recipe);
            recipeCatalog.put(recipeOutputId, newRecipeOrRecipeList);
        }

        @Override
        public String describe() {
            return "Adding shapeless recipe example for " + recipeOutputName + " with id " + recipeOutputId;
        }

        @Override
        public String describeUndo() {
            return "Reverting adding shapeless recipe example for " + recipeOutputName + " with id " + recipeOutputId;
        }
    }
}
