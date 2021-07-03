package targoss.aspecttweaker.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.util.ResourceLocation;
import targoss.aspecttweaker.AspectTweaker;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.common.lib.research.ResearchManager;

/**
 * This replaces CommonInternals.jsonLocs. It anticipates that its values() method will
 * be called whenever ResearchManager.parseAllResearch() is called. By returning an
 * empty array of values, we override Thaumcraft's research parsing.
 */
public class JsonLocsWrapper extends HashMap<String, ResourceLocation> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Need to set this upon init.
     */
    public File researchOverrideJsonDir;
    
    /**
     * This is anticipated to be called whenever ResearchManager.parseAllResearch() is called. By returning an
     * empty array of values, we override its functionality.
     */
    @Override
    public Collection<ResourceLocation> values() {
        parseAllResearch();
        return new HashMap<String, ResourceLocation>().values();
    }
    
    public Method parseResearchJsonMethod;
    public Method getParseResearchJsonMethod() {
        try {
            parseResearchJsonMethod = ResearchManager.class.getDeclaredMethod("parseResearchJson", JsonObject.class);
            parseResearchJsonMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parseResearchJsonMethod;
    }
    
    public Method addResearchToCategoryMethod;
    public Method getAddResearchToCategoryMethod() {
        try {
            addResearchToCategoryMethod = ResearchManager.class.getDeclaredMethod("addResearchToCategory", ResearchEntry.class);
            addResearchToCategoryMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addResearchToCategoryMethod;
    }
    
    public void initResearchJsonFromInputStream(InputStream stream, String researchJsonLocation) {
        InputStreamReader reader = new InputStreamReader(stream);
        JsonObject researchOverrideJsonObj = (new JsonParser()).parse(reader).getAsJsonObject();
        JsonArray entries = researchOverrideJsonObj.get("entries").getAsJsonArray();
        int i = 0;
        for (JsonElement element : entries) {
            i++;
            JsonObject entry = element.getAsJsonObject();
            try {
                ResearchEntry researchEntry = (ResearchEntry)getParseResearchJsonMethod().invoke(null, entry);
                getAddResearchToCategoryMethod().invoke(null, researchEntry);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                i--;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                i--;
            } catch (Exception e) {
                e.printStackTrace();
                AspectTweaker.LOGGER.warn("Invalid research entry [" + i + "] found in " + researchJsonLocation);
                i--;
            } 
        } 
        AspectTweaker.LOGGER.info("Loaded " + i + " research entries from " + researchJsonLocation);
    }
    
    protected static void recurseFilesAndDirectories(File directory, Map<String, File> files, Map<String, File> directories) throws IOException {
        for (File file : directory.listFiles()) {
            String canonicalPath = file.getCanonicalPath();
            if (file.isDirectory()) {
                if (!directories.containsKey(canonicalPath)) {
                    directories.put(canonicalPath, file);
                    recurseFilesAndDirectories(file, files, directories);
                }
            } else {
                files.put(canonicalPath, file);
            }
        }
    }
    
    protected static Collection<File> recurseFiles(File directory) throws IOException {
        Map<String, File> files = new HashMap<String, File>();
        recurseFilesAndDirectories(directory, files, new HashMap<String, File>());
        return files.values();
    }
    
    /**
        For the full path: [minecraft folder]/config/aspecttweaker/overrides/research/assets/thaumcraft/research/alchemy.json
        Returns something like: /assets/thaumcraft/research/alchemy.json
    */
    public static String getOverrideFileAsResourcePath(File overrideFile, File baseDirectory) {
        File child = overrideFile;
        String resourcePath = "";
        while (child != null && !child.equals(baseDirectory)) {
            resourcePath = "/" + child.getName() + resourcePath;
            child = child.getParentFile();
        }
        return resourcePath;
    }

    public void parseAllResearch() {
        researchOverrideJsonDir.mkdirs();
        // Parse the research overrides first. Check the vanilla Thaumcraft research second, and don't load a json if one with the same name has already been loaded.
        HashSet<String> researchCategoriesParsed = new HashSet<String>();
        Collection<File> candidateResarchFiles = new ArrayList<File>();
        try {
            candidateResarchFiles = recurseFiles(researchOverrideJsonDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File researchOverrideJsonFile : candidateResarchFiles) {
            if (researchOverrideJsonFile.isDirectory()) {
                continue;
            }
            if (!researchOverrideJsonFile.getName().endsWith(".json")) {
                continue;
            }
            try {
                FileInputStream stream = new FileInputStream(researchOverrideJsonFile);
                String overrideResourcePath = getOverrideFileAsResourcePath(researchOverrideJsonFile, researchOverrideJsonDir);
                initResearchJsonFromInputStream(stream, "file: " + researchOverrideJsonFile.toString() + " (overrides resource location: " + overrideResourcePath + ")");
                researchCategoriesParsed.add(overrideResourcePath);
            } catch (Exception e) {
                e.printStackTrace();
                AspectTweaker.LOGGER.warn("Failed to load research file: " + researchOverrideJsonFile.toString());
            }
        }
        // This is a re-implementation of Thaumcraft's asset loading
        for (ResourceLocation loc : super.values()) {
            String resourceLocationStringFull = "/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath();
            if (!resourceLocationStringFull.endsWith(".json")) {
                resourceLocationStringFull = resourceLocationStringFull + ".json";
            }
            if (researchCategoriesParsed.contains(resourceLocationStringFull)) {
                AspectTweaker.LOGGER.info("Research resource location NOT loaded because a suitable override json was already found: " + resourceLocationStringFull);
                continue;
            }
            InputStream stream = ResearchManager.class.getResourceAsStream(resourceLocationStringFull);
            if (stream != null) {
                initResearchJsonFromInputStream(stream, "resource location: " + resourceLocationStringFull);
                researchCategoriesParsed.add(resourceLocationStringFull);
            } else {
                AspectTweaker.LOGGER.warn("Research resource location not found: " + loc.toString());
            }
        }
    }
}
