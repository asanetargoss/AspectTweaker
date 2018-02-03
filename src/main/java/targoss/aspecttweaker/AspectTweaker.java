package targoss.aspecttweaker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.aspecttweaker.api.AspectBracketHandler;
import targoss.aspecttweaker.api.Aspects;
import targoss.aspecttweaker.api.IAspect;
import targoss.aspecttweaker.api.TCAspect;
import targoss.aspecttweaker.event.ErrorTrackingLogger;
import targoss.aspecttweaker.event.EventFiringTweaker;
import targoss.aspecttweaker.event.TweakerLoadEvent;
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
    	MinecraftForge.EVENT_BUS.register(this);
    	LOGGER = event.getModLog();
    	
    	// Annotated classes need to be manually registered
    	MineTweakerAPI.registerClass(IAspect.class);
    	MineTweakerAPI.registerClass(TCAspect.class);
    	MineTweakerAPI.registerClass(Aspects.class);
    	MineTweakerAPI.registerClass(AspectBracketHandler.class);
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
    	
    	// Get what the aspect maps look like before
    	if (LoadState.firstLoad) {
    		LoadState.firstLoad = false;
    		
    		Map<String, AspectList> objectTags = new HashMap<String, AspectList>();
    		objectTags.putAll(CommonInternals.objectTags);
    		ThaumcraftSnapshot.objectTags = objectTags;
    		
    		Map<String, int[]> groupedObjectTags = new HashMap<String, int[]>();
    		groupedObjectTags.putAll(CommonInternals.groupedObjectTags);
    		ThaumcraftSnapshot.groupedObjectTags = groupedObjectTags;
    	}

    	// Clears actions in preparation for them to be added by scripts
    	Aspects.clearAppliedActions();
    }
    
    @SubscribeEvent
    public void onTweakerLoadPost(TweakerLoadEvent.Post event) {
		updateThaumcraftAspects();
    }
    
    /** With the map of changed aspects and changed recipes from the
	 * tweaker script, re-calculate aspect values, by applying
	 * Thaumcraft's default aspects and then applying script mappings
	 * on top of it.
	 */
    public static void updateThaumcraftAspects() {
    	// Work with our own copy of the maps, to reduce issues with things being overridden
    	// Set the maps initially to the defaults provided by the snapshot
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
}
