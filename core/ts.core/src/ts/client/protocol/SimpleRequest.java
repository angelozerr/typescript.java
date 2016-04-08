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

import ts.TypeScriptException;

/**
 * Client-initiated request message
 */
public class SimpleRequest extends Request<JsonObject> {

	private JsonObject response;

	public SimpleRequest(CommandNames command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	public SimpleRequest(String command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	@Override
	public boolean complete(JsonObject response) {
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
		if (!response.getBoolean("success", true)) {
			throw new TypeScriptException(response.getString("message", ""));
		}
		return response;
	}

}
