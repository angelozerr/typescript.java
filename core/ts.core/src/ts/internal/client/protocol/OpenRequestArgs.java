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

/**
 * Information found in an "open" request.
 */
public class OpenRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param file
	 * @param fileContent
	 *            Used when a version of the file content is known to be more up
	 *            to date than the one on disk. Then the known content will be
	 *            used upon opening instead of the disk copy
	 */
	public OpenRequestArgs(String file, String fileContent) {
		super(file);
		if (fileContent != null) {
			super.add("fileContent", fileContent);
		}
	}

}
