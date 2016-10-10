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
 * Reload request message; value of command field is "reload". Reload contents
 * of file with name given by the 'file' argument from temporary file with name
 * given by the 'tmpfile' argument. The two names can be identical.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ReloadRequest extends FileRequest {

	public ReloadRequest(String fileName, String tmpfile, int seq) {
		super(CommandNames.Reload, new ReloadRequestArgs(fileName, tmpfile), seq);
	}
	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		// None response
	}
}
