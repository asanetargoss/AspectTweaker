package targoss.aspecttweaker.api;

import java.util.List;

import minetweaker.IBracketHandler;
import minetweaker.annotations.BracketHandler;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.type.natives.JavaMethod;
import stanhebben.zenscript.util.ZenPosition;
import targoss.aspecttweaker.AspectTweaker;
import thaumcraft.api.aspects.Aspect;

@BracketHandler
public class AspectBracketHandler implements IBracketHandler {
	@Override
	public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
		if (tokens.size() > 2 &&
				tokens.get(0).getValue().equals("aspect") &&
				tokens.get(1).getValue().equals(":")
				) {
			StringBuilder aspectName = new StringBuilder();
			for (int i = 2; i < tokens.size(); i++) {
				aspectName.append(tokens.get(i).getValue());
			}
			return new AspectReferenceSymbol(environment, aspectName.toString());
		}
		
		return null;
	}
	
	public static IAspect getAspect(String aspectName) {
		Aspect aspect = Aspect.aspects.get(aspectName);
		if (aspect == null) {
			throw new IllegalArgumentException("Unknown aspect '" + aspectName + "'");
		}
		
		return new TCAspect(aspect);
	}
	
	private static class AspectReferenceSymbol implements IZenSymbol {
		private final IEnvironmentGlobal environment;
		private final String aspectName;
		public AspectReferenceSymbol(IEnvironmentGlobal environment, String aspectName) {
			this.environment = environment;
			this.aspectName = aspectName;
		}
		
		@Override
		public IPartialExpression instance(ZenPosition position) {
			IJavaMethod aspectGetter = JavaMethod.get(environment, AspectBracketHandler.class, "getAspect", String.class);
			return new ExpressionCallStatic(position, environment, aspectGetter, new ExpressionString(position, aspectName));
		}
	}
}
