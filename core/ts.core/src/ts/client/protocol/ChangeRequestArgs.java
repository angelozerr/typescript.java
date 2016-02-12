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
 * Arguments for change request message.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ChangeRequestArgs extends FormatRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param line
	 * @param offset
	 * @param endLine
	 * @param endOffset
	 * @param insertString
	 *            Optional string to insert at location (file, line, offset).
	 */
	public ChangeRequestArgs(String fileName, int line, int offset, int endLine, int endOffset, String insertString) {
		super(fileName, line, offset, endLine, endOffset);
		if (insertString != null) {
			super.add("insertString", insertString);
		}
	}

}
