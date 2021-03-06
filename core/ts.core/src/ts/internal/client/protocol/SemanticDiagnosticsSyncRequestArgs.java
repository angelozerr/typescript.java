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
package ts.internal.client.protocol;

/**
 * Arguments for SemanticDiagnosticsSync messages.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.ts
 *
 */
public class SemanticDiagnosticsSyncRequestArgs extends FileRequestArgs {

	/**
	 * The file for the request (absolute pathname required).
	 */
	private final Boolean includeLinePosition;

	public SemanticDiagnosticsSyncRequestArgs(String file, Boolean includeLinePosition) {
		super(file, null);
		this.includeLinePosition = includeLinePosition;
	}

	public Boolean getIncludeLinePosition() {
		return includeLinePosition;
	}
}
