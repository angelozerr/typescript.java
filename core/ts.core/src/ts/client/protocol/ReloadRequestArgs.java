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
 * Arguments for reload request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ReloadRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param tmpfile
	 *            Name of temporary file from which to reload file contents. May
	 *            be same as file.
	 */
	public ReloadRequestArgs(String fileName, String tmpfile) {
		super(fileName);
		super.add("tmpfile", tmpfile);
	}

}
