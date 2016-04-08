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
 * Change request message; value of command field is "change". Update the
 * server's view of the file named by argument 'file'. Server does not currently
 * send a response to a change request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ChangeRequest extends FileLocationRequest {

	public ChangeRequest(String fileName, int line, int offset, int endLine, int endOffset, String insertString) {
		super(CommandNames.Change, new ChangeRequestArgs(fileName, line, offset, endLine, endOffset, insertString));
	}

}
