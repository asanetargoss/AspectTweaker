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
