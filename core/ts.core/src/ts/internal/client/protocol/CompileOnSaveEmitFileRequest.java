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
import ts.client.CommandNames;

/**
 * Request to recompile the file. All generated outputs (.js, .d.ts or .js.map
 * files) is written on disk.
 */
public class CompileOnSaveEmitFileRequest extends FileRequest {

	public CompileOnSaveEmitFileRequest(String fileName, Boolean forced) {
		super(CommandNames.compileOnSaveEmitFile, new CompileOnSaveEmitFileRequestArgs(fileName, forced), null);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {

	}

}
