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
package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TypeScriptException;
import ts.TypeScriptNoContentAvailableException;
import ts.client.ITypeScriptAsynchCollector;
import ts.client.ITypeScriptCollector;

/**
 * Client-initiated request message
 */
public abstract class SimpleRequest<C extends ITypeScriptCollector> extends Request<JsonObject, C> {

	private static final String NO_CONTENT_AVAILABLE = "No content available.";
	private JsonObject response;

	public SimpleRequest(CommandNames command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	public SimpleRequest(String command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	@Override
	public boolean complete(JsonObject response) {
		if (isAsynch()) {
			ITypeScriptAsynchCollector collector = (ITypeScriptAsynchCollector) super.getCollector();
			try {
				throwExceptionIfNeeded(response);
				collector.startCollect();
				collect(response);
				collector.endCollect();
			} catch (TypeScriptException e) {
				collector.onError(e);
			}
			return true;
		}
		this.response = response;
		synchronized (this) {
			this.notifyAll();
		}
		return isCompleted();
	}

	@Override
	protected boolean isCompleted() {
		return response != null;
	}

	@Override
	protected JsonObject getResult() throws Exception {
		throwExceptionIfNeeded(response);
		return response;
	}

	private void throwExceptionIfNeeded(JsonObject response) throws TypeScriptException {
		if (!response.getBoolean("success", true)) {
			String message = response.getString("message", "");
			if (NO_CONTENT_AVAILABLE.equals(message)) {
				throw new TypeScriptNoContentAvailableException(message);
			}
			throw new TypeScriptException(message);
		}
	}

}
