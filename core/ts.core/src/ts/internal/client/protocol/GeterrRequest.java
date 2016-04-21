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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ts.client.geterr.ITypeScriptGeterrCollector;

/**
 * Geterr request; value of command field is "geterr". Wait for delay
 * milliseconds and then, if during the wait no change or reload messages have
 * arrived for the first file in the files list, get the syntactic errors for
 * the file, field requests, and then get the semantic errors for the file.
 * Repeat with a smaller delay for each subsequent file on the files list. Best
 * practice for an editor is to send a file list containing each file that is
 * currently visible, in most-recently-used order.
 */
public class GeterrRequest extends Request<JsonArray, ITypeScriptGeterrCollector> {

	private final static int EVENT_INIT = 0;
	private final static int EVENT_SYNTAX_DIAG = 4;
	private final static int EVENT_SEMANTIC_DIAG = 16;
	private final static int EVENT_FINAL = 20;

	private final Map<String, Integer> stateFiles;
	private final JsonArray result;
	private int delay;

	public GeterrRequest(String[] files, int delay, ITypeScriptGeterrCollector collector) {
		super(CommandNames.Geterr, new GeterrRequestArgs(files, delay), null);
		this.stateFiles = createStateFiles(files);
		this.delay = delay;
		this.result = new JsonArray();
	}

	private Map<String, Integer> createStateFiles(String[] files) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < files.length; i++) {
			map.put(files[i], EVENT_INIT);
		}
		return map;
	}

	public Collection<String> getFiles() {
		return stateFiles.keySet();
	}

	public int getDelay() {
		return delay;
	}

	public void dispose(String file) {
		synchronized (stateFiles) {
			stateFiles.remove(file);
		}
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public boolean complete(JsonObject response) {
		result.add(response);

		String event = response.getString("event", null);
		JsonObject body = response.get("body").asObject();
		String file = body.getString("file", null);
		Integer mask = stateFiles.get(file);
		mask = mask.intValue() | ("syntaxDiag".equals(event) ? EVENT_SYNTAX_DIAG : EVENT_SEMANTIC_DIAG);
		if (mask == EVENT_FINAL) {
			dispose(file);
			return true;
		} else {
			synchronized (stateFiles) {
				stateFiles.put(file, mask);
			}
			return false;
		}
	}

	@Override
	protected boolean isCompleted() {
		return stateFiles.isEmpty();
	}

	@Override
	protected JsonArray getResult() throws Exception {
		return result;
	}

	@Override
	public void collect(JsonObject response) {
		
	}
}
