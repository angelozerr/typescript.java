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

import com.eclipsesource.json.JsonObject;

/**
 * A TypeScript Server message
 */
public class Message extends JsonObject {

	private static final String SEQ_FIELD = "seq";
	private static final String TYPE_FIELD = "type";

	public Message(int seq, String type) {
		super.add(SEQ_FIELD, seq);
		super.add(TYPE_FIELD, type);
	}

	public int getSeq() {
		return getInt(SEQ_FIELD, -1);
	}

	public String getType() {
		return getString(TYPE_FIELD, null);
	}
}
