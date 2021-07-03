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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class ClassPatcher implements IClassTransformer {
    /**
     * @param name
     * Name of class to be transformed. Used in debug.
     * @param basicClass
     * Input bytes
     * @param flags
     * ClassWriter flag int. Generally ClassWriter.COMPUTE_MAXS, or 0 for simple patches.
     * @return
     * Output bytes
     */
    public final byte[] transformClass(String name, byte[] basicClass, int flags) {
        if (enableDebug()) {
            AspectTweakerCoremod.LOGGER.debug("Attempt to patch class '" +
                    name + "' started by '" +
                    this.getClass().getName() + "'");
        }
        
        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode visitor = new ClassNode();
            reader.accept(visitor, 0);
            
            transformClassNode(visitor);
            
            ClassWriter writer = new ClassWriter(flags);
            visitor.accept(writer);
            byte[] newClass = writer.toByteArray();
            
            if (enableDebug()) {
                AspectTweakerCoremod.LOGGER.debug(
                        "Outputting result of patch to class '" +
                        name + "' made by '" +
                        this.getClass().getName() + "'"
                        );
                AspectTweakerCoremod.logBytesToDebug(newClass);
            }

            return newClass;
        }
        catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter wrapper = new PrintWriter(stringWriter);
            e.printStackTrace(wrapper);
            
            AspectTweakerCoremod.LOGGER.error(
                    "Error occurred when attempting to patch class '" +
                    name + "' using '" +
                    this.getClass().getName() + "'." +
                    "The patch has been aborted.");
            if (enableDebug()) {
                AspectTweakerCoremod.LOGGER.debug(
                        "Debug is enabled. The bytecode of the unpatched " +
                        "class will follow the stack trace.");
            };
            AspectTweakerCoremod.LOGGER.error(stringWriter.toString());
            
            if (enableDebug()) {
                AspectTweakerCoremod.LOGGER.debug(
                        "Outputting unpatched class '" +
                        name + "'");
                AspectTweakerCoremod.logBytesToDebug(basicClass);
            }
        }
        
        return basicClass;
    }
    
    /**
     * Whether to print the transformed class bytes to the console.
     */
    public boolean enableDebug() {
        return false;
    }
    
    public abstract void transformClassNode(ClassNode classNode);
}
