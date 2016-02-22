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

import ts.utils.JsonHelper;

/**
 * Arguments for completion details request.
 *
 */
public class CompletionDetailsRequestArgs extends FileLocationRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param line
	 * @param offset
	 * @param entryNames
	 *            Names of one or more entries for which to obtain details.
	 */
	public CompletionDetailsRequestArgs(String fileName, int line, int offset, String[] entryNames) {
		super(fileName, line, offset);
		if (entryNames != null) {
			super.add("entryNames", JsonHelper.toJson(entryNames));
		}
	}

}
