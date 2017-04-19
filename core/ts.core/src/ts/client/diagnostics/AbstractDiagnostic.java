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

/**
 * Item of diagnostic information found in a DiagnosticEvent message.
 *
 */
public abstract class AbstractDiagnostic implements IDiagnostic {

	/**
	 * Text of diagnostic message.
	 */
	private String text;

	/**
	 * The error code of the diagnostic message.
	 */
	private Integer code;

	private String category;

	public String getText() {
		return text;
	}

	public Integer getCode() {
		return code;
	}

	public DiagnosticCategory getCategory() {
		return DiagnosticCategory.getCategory(category);
	}
}
