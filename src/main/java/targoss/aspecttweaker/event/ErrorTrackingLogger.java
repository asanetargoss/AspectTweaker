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
