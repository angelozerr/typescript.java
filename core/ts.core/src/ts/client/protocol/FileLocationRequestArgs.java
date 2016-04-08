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
 * Instances of this interface specify a location in a source file: (file, line,
 * character offset), where line and character offset are 1-based.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileLocationRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param line
	 *            The line number for the request (1-based).
	 * @param offset
	 *            The character offset (on the line) for the request (1-based).
	 */
	public FileLocationRequestArgs(String fileName, int line, int offset) {
		super(fileName);
		super.add("line", line);
		super.add("offset", offset);
	}

}
