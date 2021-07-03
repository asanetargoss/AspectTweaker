/*
 * MIT License
 * 
 * Copyright (c) 2018 asanetargoss
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package targoss.aspecttweaker;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.aspecttweaker.api.AspectBracketHandler;
import targoss.aspecttweaker.api.Aspects;
import targoss.aspecttweaker.api.IAspect;
import targoss.aspecttweaker.api.RecipeExamples;
import targoss.aspecttweaker.api.TCAspect;
import targoss.aspecttweaker.coremod.AspectTweakerCoremod;
import targoss.aspecttweaker.event.ErrorTrackingLogger;
import targoss.aspecttweaker.event.EventFiringTweaker;
import targoss.aspecttweaker.event.TweakerLoadEvent;
import targoss.aspecttweaker.wrapper.JsonLocsWrapper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.internal.CommonInternals;

@Mod(modid = AspectTweaker.MODID, version = AspectTweaker.VERSION, dependencies = AspectTweaker.DEPENDENCIES)
public class AspectTweaker
{
    public static final String MODID = "aspecttweaker";
    public static final String VERSION = "0.1.0";
    public static final String DEPENDENCIES = 
    		"required-after:MineTweaker3;" +
    		"required-after:thaumcraft;";
    
    public static Logger LOGGER;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!AspectTweakerCoremod.isCoremodInitialized()){
            throw new ModStateException("The coremod at '" +
                    AspectTweakerCoremod.class.getName() +
                    "' did not run");
        }
        
    	MinecraftForge.EVENT_BUS.register(this);
    	LOGGER = event.getModLog();
    	
    	// Annotated classes need to be manually registered
    	// Interfaces
    	MineTweakerAPI.registerClass(IAspect.class);
    	// Implementations
    	MineTweakerAPI.registerClass(TCAspect.class);
    	// Static class utilities
    	MineTweakerAPI.registerClass(Aspects.class);
        MineTweakerAPI.registerClass(RecipeExamples.class);
        // Bracket handlers
    	MineTweakerAPI.registerClass(AspectBracketHandler.class);
    	
    	// Override jsonLocs so `/tc reload` also checks our custom folder location
    	HashMap<String, ResourceLocation> oldJsonLocs = CommonInternals.jsonLocs;
    	JsonLocsWrapper newJsonLocs = new JsonLocsWrapper();
    	newJsonLocs.researchOverrideJsonDir = new File(new File(new File(event.getModConfigurationDirectory(), MODID), "overrides"), "research");
    	CommonInternals.jsonLocs = newJsonLocs;
    	CommonInternals.jsonLocs.putAll(oldJsonLocs);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    		throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    	/* Wrap MineTweakerAPI.tweaker.load(), MineTweakerImplementationAPI.logger.logError()
    	 * with EventFiringTweaker and ErrorTrackingLogger respectively
    	 */
    	Field modifiersField = Field.class.getDeclaredField("modifiers");
    	modifiersField.setAccessible(true);
    	
    	Field tweakerField = MineTweakerAPI.class.getField("tweaker");
    	modifiersField.setInt(tweakerField, tweakerField.getModifiers() & ~Modifier.FINAL);
    	tweakerField.set(null, new EventFiringTweaker(MineTweakerAPI.tweaker));
    	Field loggerField = MineTweakerImplementationAPI.class.getField("logger");
    	modifiersField.setInt(loggerField, tweakerField.getModifiers() & ~Modifier.FINAL);
    	loggerField.set(null, new ErrorTrackingLogger(MineTweakerImplementationAPI.logger));
    }
    
    @SubscribeEvent
    public void onTweakerLoadPre(TweakerLoadEvent.Pre event) {
    	LoadState.errorLogged = false;
    	
    	// Get what the aspect maps/recipe catalog look like before
    	if (LoadState.firstLoad) {
    		LoadState.firstLoad = false;
    		
    		Map<String, AspectList> objectTags = new HashMap<String, AspectList>();
    		objectTags.putAll(CommonInternals.objectTags);
    		ThaumcraftSnapshot.objectTags = objectTags;
    		
    		Map<String, int[]> groupedObjectTags = new HashMap<String, int[]>();
    		groupedObjectTags.putAll(CommonInternals.groupedObjectTags);
    		ThaumcraftSnapshot.groupedObjectTags = groupedObjectTags;
    		
    		HashMap<String, Object> craftingRecipeCatalog = new HashMap<String, Object>();
    		craftingRecipeCatalog.putAll(CommonInternals.craftingRecipeCatalog);
    		ThaumcraftSnapshot.craftingRecipeCatalog = craftingRecipeCatalog;
    	}

    	// Clears actions in preparation for them to be added by scripts
    	Aspects.clearAppliedActions();
    	RecipeExamples.clearAppliedActions();
    }
    
    @SubscribeEvent
    public void onTweakerLoadPost(TweakerLoadEvent.Post event) {
        // We will work with our own copy of the maps, to reduce issues with things being overridden
        // Set the maps initially to the defaults provided by the snapshot
		updateThaumcraftAspects();
		updateRecipeExamples();
    }
    
    /** With the map of changed aspects and changed recipes from the
	 * tweaker script, re-calculate aspect values, by applying
	 * Thaumcraft's default aspects and then applying script mappings
	 * on top of it.
	 */
    public static void updateThaumcraftAspects() {
    	ConcurrentHashMap<String, AspectList> objectTags = new ConcurrentHashMap<String, AspectList>();
    	objectTags.putAll(ThaumcraftSnapshot.objectTags);
    	/* Honestly, I have no idea what groupedObjectTags should be
    	 * used for. I am basically ignoring it and keeping the
    	 * original values.
    	 */
    	ConcurrentHashMap<String, int[]> groupedObjectTags = new ConcurrentHashMap<String, int[]>();
    	groupedObjectTags.putAll(ThaumcraftSnapshot.groupedObjectTags);
    	
    	// Apply new aspect changes on top of the initial snapshot
    	Aspects.applyToObjectTags(objectTags);
    	
    	// We're done; install the new mappings
    	CommonInternals.objectTags = objectTags;
    	CommonInternals.groupedObjectTags = groupedObjectTags;
    }
    
    public static void updateRecipeExamples() {
        HashMap<String, Object> craftingRecipeCatalog = new HashMap<String, Object>();
        craftingRecipeCatalog.putAll(ThaumcraftSnapshot.craftingRecipeCatalog);
        
        // Apply new aspect changes on top of the initial snapshot
        RecipeExamples.applyToCraftingRecipeCatalog(craftingRecipeCatalog);
        
        // We're done; install the new mappings
        CommonInternals.craftingRecipeCatalog = craftingRecipeCatalog;
    }
}
