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

/**
 * Signature help request; value of command field is "signatureHelp". Given a
 * file location (file, line, col), return the signature help.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class SignatureHelpRequest extends FileLocationRequest {

	public SignatureHelpRequest(String fileName, int line, int offset) {
		super(CommandNames.SignatureHelp, new SignatureHelpRequestArgs(fileName, line, offset));
	}
	
	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		// None response
	}
}
