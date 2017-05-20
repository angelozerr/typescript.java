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
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.ts
 *
 */
public class GetRefactorCodeActionsRequestArgs extends FileLocationRequestArgs {

	/* The kind of the applicable refactor */
	private String refactorName;

	public GetRefactorCodeActionsRequestArgs(String file, int position, String refactorName) {
		super(file, position);
		this.refactorName = refactorName;
	}

	public GetRefactorCodeActionsRequestArgs(String file, int line, int offset, String refactorName) {
		super(file, line, offset);
		this.refactorName = refactorName;
	}

	public String getRefactorName() {
		return refactorName;
	}
}
