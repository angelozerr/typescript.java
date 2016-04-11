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

/**
 * Arguments for FileRequest messages.
 */
public class FileRequestArgs extends JsonObject {

	/**
	 * 
	 * @param file
	 *            The file for the request (absolute pathname required).
	 */
	public FileRequestArgs(String file) {
		super.add("file", file);
	}

}
