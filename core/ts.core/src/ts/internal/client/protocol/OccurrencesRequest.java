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
import ts.client.occurrences.ITypeScriptOccurrencesCollector;

/**
 * Get occurrences request; value of command field is "occurrences". Return
 * response giving spans that are relevant in the file at a given line and
 * column.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class OccurrencesRequest extends FileLocationRequest<ITypeScriptOccurrencesCollector> {

	public OccurrencesRequest(String fileName, int line, int offset, ITypeScriptOccurrencesCollector collector) {
		super(CommandNames.Occurrences, new FileLocationRequestArgs(fileName, line, offset));
		super.setCollector(collector);
	}

	public void collect(JsonObject response)
			throws TypeScriptException {
		ITypeScriptOccurrencesCollector collector = super.getCollector();
		JsonArray body = response.get("body").asArray();
		JsonObject occurrence = null;
		String file = null;
		JsonObject start = null;
		JsonObject end = null;
		boolean isWriteAccess = false;
		for (JsonValue b : body) {
			occurrence = b.asObject();
			file = occurrence.getString("file", null);
			start = occurrence.get("start").asObject();
			end = occurrence.get("end").asObject();
			isWriteAccess = occurrence.getBoolean("isWriteAccess", false);
			collector.addOccurrence(file, start.getInt("line", -1), start.getInt("offset", -1), end.getInt("line", -1),
					end.getInt("offset", -1), isWriteAccess);

		}
	}

}
