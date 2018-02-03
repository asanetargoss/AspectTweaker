# Aspect Tweaker
Aspect Tweaker is a CraftTweaker add-on for Minecraft 1.10.2 which allows you to change the aspects of items in Thaumcraft 6.

## Usage
Currently, only items are supported (and blocks), not ore dictionaries or mobs.

Here is an example script showing how to use Aspect Tweaker in a CraftTweaker script:

```
import aspecttweaker.Aspects;
import aspecttweaker.IAspect;

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
```

# Developing
## Dependencies
Download the normal/obfuscated 1.10.2 versions of these mods and put them in /libs:

* [CraftTweaker](https://minecraft.curseforge.com/projects/crafttweaker)
* [Thaumcraft](https://minecraft.curseforge.com/projects/thaumcraft?gameCategorySlug=mc-mods&projectID=223628)
* [Baubles](https://minecraft.curseforge.com/projects/baubles?gameCategorySlug=mc-mods&projectID=227083)
* [CodeChicken Core](https://minecraft.curseforge.com/projects/codechicken-core-1-8?gameCategorySlug=mc-mods&projectID=243822)
* [CodeChickenLib](https://minecraft.curseforge.com/projects/codechicken-lib-1-8)

## Setup
* Call "./gradlew setupDecompWorkspace" from command line in root directory
* Call "./gradlew eclipse" (or equivalent for idea) from command line in root directory

## Running
The CraftTweaker mcp mappings are old which can lead to crashes in a 1.10.2 development workspace, so you must remove/disable scripts when entering a world and then use "/minetweaker reload" with the scripts re-enabled. Also, don't craft things.

## Compiling
Call "./gradlew assemble" from command line in root directory, and fetch output from build/libs