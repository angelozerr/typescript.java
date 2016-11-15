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

public class CodeFixRequestArgs extends FileRequestArgs {

	public CodeFixRequestArgs(String file, int startLine, int startOffset, int endLine, int endOffset,
			String[] errorCodes) {
		super(file);
		super.add("startLine", startLine);
		super.add("startOffset", startOffset);
		super.add("endLine", endLine);
		super.add("endOffset", endOffset);

		if (errorCodes != null) {
			JsonArray codes = new JsonArray();
			super.add("errorCodes", codes);
			for (int i = 0; i < errorCodes.length; i++) {
				codes.add(errorCodes[i]);
			}
		}
	}

}
