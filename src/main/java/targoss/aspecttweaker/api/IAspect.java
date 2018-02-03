package targoss.aspecttweaker.api;

import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenOperator;
import thaumcraft.api.aspects.Aspect;

@ZenClass("aspecttweaker.IAspect")
public interface IAspect {
	@ZenGetter("name")
	String getName();
	
	@ZenGetter("amount")
	int getAmount();
	
	@ZenOperator(OperatorType.MUL)
	@ZenMethod
	IAspect amount(int amount);
	
	Aspect getAspect();
}
