/*
 * MIT License
 * 
 * Copyright (c) 2021 asanetargoss
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
package targoss.aspecttweaker.coremod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.aspecttweaker.coremod.transform.TThaumcraftApiHelper;

@IFMLLoadingPlugin.Name(value = "Aspect Tweaker Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.aspecttweaker.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class AspectTweakerCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }
    
    public static boolean obfuscated = false;
    public static boolean TAN_LOADED = false;
    
    public static final Logger LOGGER = LogManager.getLogger("Aspect Tweaker Coremod");
    
	@Override
	public String[] getASMTransformerClass() {
	    coremodInitialized = true;
		return new String[]{
            TThaumcraftApiHelper.class.getName()
		};
	}

	@Override
	public String getModContainerClass() {
		return Container.class.getName();
	}
	
	public static class Container extends DummyModContainer {
		public Container() {
			super(new ModMetadata());
			ModMetadata meta = getMetadata();
			meta.modId = "aspecttweaker-coremod";
			meta.name = "Aspect Tweaker Coremod";
			meta.version = targoss.aspecttweaker.AspectTweaker.VERSION;
			meta.credits = "";
			meta.authorList = Arrays.asList("asanetargoss");
			meta.description = "The secret sauce.";
			meta.url = "";
			meta.screenshots = new String[0];
			meta.logoFile = "";
		}
		
		public boolean registerBus(EventBus bus, LoadController controller) {
			bus.register(this);
			return true;
		}
		
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		obfuscated = (Boolean)(data.get("runtimeDeobfuscationEnabled"));
		LOGGER.debug("runtimeDeobfuscationEnabled: "+obfuscated);
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
	
	public static void logBytesToDebug(byte[] bytes) {
	    StringWriter stringWriter = new StringWriter();
        TraceClassVisitor traceVisitor = new TraceClassVisitor(new PrintWriter(stringWriter));
        (new ClassReader(bytes)).accept(traceVisitor, 0);
        LOGGER.debug(stringWriter.getBuffer());
	}

}
