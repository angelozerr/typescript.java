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
import ts.client.definition.ITypeScriptDefinitionCollector;

/**
 * Go to definition request; value of command field is "definition". Return
 * response giving the file locations that define the symbol found in file at
 * location line, col.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class DefinitionRequest extends FileLocationRequest<ITypeScriptDefinitionCollector> {

	public DefinitionRequest(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector) {
		super(CommandNames.Definition, new FileLocationRequestArgs(fileName, line, offset));
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		ITypeScriptDefinitionCollector collector = super.getCollector();
		JsonArray items = response.get("body").asArray();
		JsonObject def = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue item : items) {
			def = (JsonObject) item;
			start = def.get("start").asObject();
			end = def.get("end").asObject();
			collector.addDefinition(def.getString("file", null), start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
	}

}
