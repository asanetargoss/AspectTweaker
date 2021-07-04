# Aspect Tweaker

Aspect Tweaker is a CraftTweaker add-on for Minecraft 1.10.2 and Thaumcraft 6.

The mod allows you to:

- Add/remove aspects from items/blocks (aspects on ore dictionaries is not implemented)
- Modify recipe examples in the Thaumonomicon for shaped/shapeless crafting table recipes
- Override the Thaumcraft research tree

In addition, the mod also fixes an item meta crafting bug with cauldrons.

## CraftTweaker Usage

Here is an example script showing the CraftTweaker functionality. This script can be edited and then reloaded in-game using `/minetweaker reload`.

```ZenScript
import aspecttweaker.Aspects;
import aspecttweaker.IAspect;
import aspectttweaker.RecipeExamples;

// Set aspects for items
Aspects.setAspects(<minecraft:snowball>, [<aspect:ignis>]);
Aspects.setAspects(<minecraft:diamond>, [<aspect:alienis>*3]);
Aspects.removeAspects(<minecraft:log:2>);

// Aspects belong to the type IAspect
val primals = [<aspect:aer>,<aspect:terra>,<aspect:ignis>,<aspect:aqua>,<aspect:ordo>,<aspect:perditio>] as IAspect[];
Aspects.setAspects(<minecraft:redstone>, primals);

// <aspect:aer>.name gives "Aer", and so on
print("All of the primals:");
for primal in primals {
    print(primal.name);
}

// <aspect:terra>.amount gives 1, and so on
print("A lot of terra:");
print((<aspect:terra>*687).amount);

// New in 0.2.0: Merge two arrays of aspects into one
val merged = Aspects.merge([<aspect:terra>*2,<aspect:ignis>], [<aspect:terra>,<aspect:tenebrae>]);
print("3 Terra, plus Ignis and Tenebrae:");
for aspect in merged {
    print(aspect.name + ": " + aspect.amount);
}

// New in 0.2.0: Change recipe examples in the Thaumonomicon for crafting table recipes.
// If you change a crafting table recipe that shows up in the Thaumonomicon, or if you want to add new crafting table recipe examples to the Thaumonomicon, you must change the recipe examples manually, using one of the functions provided below.
RecipeExamples.addShaped(<minecraft:diamond>, [
    [null, <minecraft:dirt>, null],
    [<minecraft:dirt>, null, <minecraft:dirt>],
    [null, <minecraft:dirt>, null]
], "dirt_to_diamond_shaped");
RecipeExamples.addShapeless(<minecraft:diamond>, [<minecraft:dirt>,<minecraft:dirt>,<minecraft:dirt>], "op_recipes");
RecipeExamples.addShapeless(<minecraft:emerald>, [<minecraft:sand>,<minecraft:sand>,<minecraft:sand>], "op_recipes");
RecipeExamples.removeAllForId("dirt_to_diamond_shaped");
RecipeExamples.removeAllForIdMatchingItem("op_recipes", <minecraft:emerald>);
```

## Research JSON Usage

New in 0.2.0: You can override Thaumcraft research jsons by placing them in the correct folder. These jsons can be reloaded using the command `/tc reload`.

For example, if you want to override (from the Thaumcraft jar) `assets/thaumcraft/research/basics.json`, you can put your own custom version at `[minecraft folder]/config/aspecttweaker/overrides/research/assets/thaumcraft/research/basics.json`. Research jsons can also be put anywhere inside of `[minecraft folder]/config/aspecttweaker/overrides/research`, however they will only add to existing research.

For more information about the Thaumcraft research JSON format, see `assets/thaumcraft/research/_example.json.txt` inside the Thaumcraft jar. You can view the Thaumcraft jar's contents using 7-Zip, unzip, or your favorite zip archive viewer.

If you want to change the text of Thaumcraft research, you should use a resource pack to override Thaumcraft's language files (ex: `assets/thaumcraft/lang/en_US.lang`). To reload resource packs, use `F3+T` and wait a few minutes.

## Developing

### Dependencies

Download the normal/obfuscated 1.10.2 versions of these mods and put them in the /libs folder relative to the root directory, creating the folder if it does not exist:

* [CraftTweaker](https://minecraft.curseforge.com/projects/crafttweaker)
* [Thaumcraft](https://minecraft.curseforge.com/projects/thaumcraft?gameCategorySlug=mc-mods&projectID=223628)
* [Baubles](https://minecraft.curseforge.com/projects/baubles?gameCategorySlug=mc-mods&projectID=227083)
* [CodeChicken Core](https://minecraft.curseforge.com/projects/codechicken-core-1-8?gameCategorySlug=mc-mods&projectID=243822)
* [CodeChickenLib](https://minecraft.curseforge.com/projects/codechicken-lib-1-8)

### Setup

* Call "./gradlew setupDecompWorkspace" from command line in root directory
* Call "./gradlew eclipse" (or equivalent for idea) from command line in root directory

### Running

The CraftTweaker mcp mappings are old which can lead to crashes in a 1.10.2 development workspace, so you must remove/disable scripts when entering a world and then use "/minetweaker reload" with the scripts re-enabled. Also, don't craft things.

### Compiling

Call "./gradlew assemble" from command line in root directory, and fetch output from build/libs
