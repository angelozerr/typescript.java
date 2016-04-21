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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ITypeScriptCompletionCollector;

/**
 * Completions request; value of command field is "completions". Given a file
 * location (file, line, col) and a prefix (which may be the empty string),
 * return the possible completions that begin with prefix.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class CompletionsRequest extends FileLocationRequest<ITypeScriptCompletionCollector> {

	private final String fileName;
	private final int line;
	private final int offset;
	private final ITypeScriptServiceClient client;

	public CompletionsRequest(String fileName, int line, int offset, String prefix,
			ITypeScriptCompletionCollector collector, ITypeScriptServiceClient client) {
		super(CommandNames.Completions, new CompletionsRequestArgs(fileName, line, offset, prefix));
		super.setCollector(collector);
		this.fileName = fileName;
		this.line = line;
		this.offset = offset;
		this.client = client;
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		ITypeScriptCompletionCollector collector = super.getCollector();
		JsonArray items = response.get("body").asArray();
		JsonObject obj = null;
		for (JsonValue item : items) {
			obj = (JsonObject) item;
			collector.addCompletionEntry(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""), obj.getString("sortText", ""), fileName, line, offset, client);
		}
	}

}
