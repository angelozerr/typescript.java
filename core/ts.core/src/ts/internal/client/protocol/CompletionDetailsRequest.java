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
import ts.client.CommandNames;
import ts.client.completions.ITypeScriptCompletionEntryDetailsCollector;
import ts.utils.JsonHelper;

/**
 * Completion entry details request; value of command field is
 * "completionEntryDetails". Given a file location (file, line, col) and an
 * array of completion entry names return more detailed information for each
 * completion entry.
 */
public class CompletionDetailsRequest extends FileLocationRequest<ITypeScriptCompletionEntryDetailsCollector> {

	public CompletionDetailsRequest(String fileName, int line, int offset, String[] entryNames,
			ITypeScriptCompletionEntryDetailsCollector collector) {
		super(CommandNames.CompletionEntryDetails,
				new CompletionDetailsRequestArgs(fileName, line, offset, entryNames));
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		ITypeScriptCompletionEntryDetailsCollector collector = super.getCollector();
		JsonArray body = response.get("body").asArray();
		if (body != null && body.size() > 0) {
			JsonObject obj = body.get(0).asObject();
			collector.setEntryDetails(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""));
			// displayParts
			JsonArray displayParts = JsonHelper.getArray(obj, "displayParts");
			JsonObject o = null;
			if (displayParts != null) {
				for (JsonValue part : displayParts) {
					o = part.asObject();
					collector.addDisplayPart(o.getString("text", ""), o.getString("kind", ""));
				}
			}
			// documentation
			JsonArray documentation = JsonHelper.getArray(obj, "documentation");
			if (documentation != null) {
				for (JsonValue part : documentation) {
					o = part.asObject();
					collector.addDisplayPart(o.getString("text", ""), o.getString("kind", ""));
				}
			}
		}
	}
}