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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ts.utils.JsonHelper;

/**
 * Arguments for geterr messages.
 */
public class GeterrRequestArgs extends JsonObject {

	/**
	 * 
	 * @param files
	 *            List of file names for which to compute compiler errors. The
	 *            files will be checked in list order.
	 * @param delay
	 *            Delay in milliseconds to wait before starting to compute
	 *            errors for the files in the file list
	 */
	public GeterrRequestArgs(String[] files, int delay) {
		super.add("files", JsonHelper.toJson(files));
		super.add("delay", delay);
	}

	public JsonArray getFiles() {
		return super.get("files").asArray();
	}

	public int getDelay() {
		return super.getInt("delay", -1);
	}

}
