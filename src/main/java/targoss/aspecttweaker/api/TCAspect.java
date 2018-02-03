package targoss.aspecttweaker.api;

import thaumcraft.api.aspects.Aspect;

public class TCAspect implements IAspect {
	private Aspect aspect;
	private int amount;
	
	public TCAspect(Aspect aspect) {
		this(aspect, 1);
	}
	
	public TCAspect(Aspect aspect, int amount) {
		this.aspect = aspect;
		this.amount = amount;
	}

	@Override
	public String getName() {
		return aspect.getName();
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public Aspect getAspect() {
		return this.aspect;
	}

	@Override
	public IAspect amount(int amount) {
		return new TCAspect(this.aspect, this.amount*amount);
	}

}
