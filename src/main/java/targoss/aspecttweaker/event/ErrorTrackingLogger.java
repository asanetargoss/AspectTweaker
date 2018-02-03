package targoss.aspecttweaker.event;

import minetweaker.api.logger.MTLogger;
import minetweaker.api.player.IPlayer;
import minetweaker.runtime.ILogger;
import targoss.aspecttweaker.LoadState;

public class ErrorTrackingLogger extends MTLogger {
	public final MTLogger delegate;
	
	public ErrorTrackingLogger(MTLogger delegate) {
		this.delegate = delegate;
	}

	@Override
	public void addLogger(ILogger logger) {
		delegate.addLogger(logger);
	}

	@Override
	public void addPlayer(IPlayer player) {
		delegate.addPlayer(player);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public void logCommand(String command) {
		delegate.logCommand(command);
	}

	@Override
	public void logError(String message, Throwable throwable) {
		LoadState.errorLogged = true;
		delegate.logError(message, throwable);
	}

	@Override
	public void logError(String message) {
		LoadState.errorLogged = true;
		delegate.logError(message);
	}

	@Override
	public void logInfo(String message) {
		delegate.logInfo(message);
	}

	@Override
	public void logWarning(String message) {
		delegate.logWarning(message);
	}

	@Override
	public void removeLogger(ILogger logger) {
		delegate.removeLogger(logger);
	}

	@Override
	public void removePlayer(IPlayer player) {
		delegate.removePlayer(player);
	}

}
