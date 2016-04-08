/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.client.protocol;

import com.eclipsesource.json.JsonObject;

import ts.client.TypeScriptTimeoutException;
import ts.internal.SequenceHelper;
import ts.internal.server.ICallbackItem;

public abstract class Request<T> extends Message implements ICallbackItem<T> {

	private static final long TIMEOUT = 10000L * 1000000L; // wait 10 second
															// before timeout.

	private long startTime;

	public Request(CommandNames command, JsonObject args, Integer seq) {
		this(command.getName(), args, seq);
	}

	public Request(String command, JsonObject args, Integer seq) {
		super(seq != null ? seq : SequenceHelper.getRequestSeq(), "request");
		super.add("command", command);
		if (args != null) {
			super.add("arguments", args);
		}
	}

	public String getCommand() {
		return super.getString("command", null);
	}

	public JsonObject getArguments() {
		return super.get("arguments").asObject();
	}

	@Override
	public final T call() throws Exception {
		this.startTime = System.nanoTime();
		while (!isCompleted()) {
			synchronized (this) {
				// wait for 200ms otherwise if we don't set ms, if completion is
				// executed several times
				// quickly (do Ctrl+Space every time), the Thread could be
				// blocked? Why?
				this.wait(5);
			}
			if ((System.nanoTime() - startTime) > TIMEOUT) {
				throw new TypeScriptTimeoutException(this, TIMEOUT);
			}
		}
		return getResult();
	}

	public long getStartTime() {
		return startTime;
	}

	protected abstract boolean isCompleted();

	protected abstract T getResult() throws Exception;

}
