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
package ts.client.protocol;

/**
 * Completions request; value of command field is "completions". Given a file
 * location (file, line, col) and a prefix (which may be the empty string),
 * return the possible completions that begin with prefix.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class CompletionsRequest extends FileLocationRequest {

	public CompletionsRequest(String fileName, int line, int offset, String prefix) {
		super(CommandNames.Completions, new CompletionsRequestArgs(fileName, line, offset, prefix));
	}

}
