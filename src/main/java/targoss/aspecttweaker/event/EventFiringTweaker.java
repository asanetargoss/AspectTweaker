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
package targoss.aspecttweaker.event;

import java.util.List;

import minetweaker.IUndoableAction;
import minetweaker.api.item.IIngredient;
import minetweaker.runtime.IScriptProvider;
import minetweaker.runtime.ITweaker;
import net.minecraftforge.common.MinecraftForge;

public class EventFiringTweaker implements ITweaker {
	
	public final ITweaker delegate;
	
	public EventFiringTweaker(ITweaker delegate) {
		this.delegate = delegate;
	}

	@Override
	public byte[] getStagedScriptData() {
		return delegate.getStagedScriptData();
	}

	@Override
	public void apply(IUndoableAction action) {
		delegate.apply(action);
	}

	@Override
	public void remove(IIngredient ingredient) {
		delegate.remove(ingredient);
	}

	@Override
	public List<IUndoableAction> rollback() {
		return delegate.rollback();
	}

	@Override
	public void setScriptProvider(IScriptProvider scriptProvider) {
		delegate.setScriptProvider(scriptProvider);
	}

	@Override
	public synchronized void load() {
		MinecraftForge.EVENT_BUS.post(new TweakerLoadEvent.Pre());
		delegate.load();
		MinecraftForge.EVENT_BUS.post(new TweakerLoadEvent.Post());
	}

	@Override
	public byte[] getScriptData() {
		return delegate.getScriptData();
	}

	@Override
	public List<IUndoableAction> getActions() {
		return delegate.getActions();
	}

	@Override
	public void enableDebug() {
		delegate.enableDebug();
	}

}
