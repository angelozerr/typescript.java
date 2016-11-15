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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.codefixes.ITypeScriptGetSupportedCodeFixesCollector;

/**
 * A request to get codes of supported code fixes.
 */
public class GetSupportedCodeFixesRequest extends SimpleRequest<ITypeScriptGetSupportedCodeFixesCollector> {

	public GetSupportedCodeFixesRequest(ITypeScriptGetSupportedCodeFixesCollector collector) {
		super(CommandNames.GetSupportedCodeFixes, null, null);
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		Gson gson = new GsonBuilder().create();
		GetSupportedCodeFixesResponse a = gson.fromJson(response.toString(), GetSupportedCodeFixesResponse.class);
		getCollector().setSupportedCodeFixes(a.getBody());
	}

}
