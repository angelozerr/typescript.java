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
 * Completion entry details request; value of command field is
 * "completionEntryDetails". Given a file location (file, line, col) and an
 * array of completion entry names return more detailed information for each
 * completion entry.
 */
public class CompletionDetailsRequest extends FileLocationRequest {

	public CompletionDetailsRequest(String fileName, int line, int offset, String[] entryNames) {
		super(CommandNames.CompletionEntryDetails, new CompletionDetailsRequestArgs(fileName, line, offset, entryNames));
	}
	
	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		// None response
	}
}