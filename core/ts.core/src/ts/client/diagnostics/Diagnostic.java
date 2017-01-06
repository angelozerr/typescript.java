/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.client.diagnostics;

import ts.client.Location;

/**
 * Item of diagnostic information found in a DiagnosticEvent message.
 *
 */
public class Diagnostic {

	/**
	 * Starting file location at which text applies.
	 */
	private Location start;

	/**
	 * The last file location at which the text applies.
	 */
	private Location end;

	/**
	 * Text of diagnostic message.
	 */
	private String text;

	/**
	 * The error code of the diagnostic message.
	 */
	private Integer code;

	private String category;

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		return end;
	}

	public String getText() {
		return text;
	}

	public Integer getCode() {
		return code;
	}

	public String getCategory() {
		return category;
	}
}
