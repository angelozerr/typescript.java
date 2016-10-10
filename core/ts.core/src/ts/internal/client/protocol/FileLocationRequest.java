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

import ts.client.CommandNames;
import ts.client.ITypeScriptCollector;

/**
 * A request whose arguments specify a file location (file, line, col).
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public abstract class FileLocationRequest<C extends ITypeScriptCollector> extends FileRequest<C> {

	public FileLocationRequest(CommandNames command, FileLocationRequestArgs args) {
		super(command, args, null);
	}

	public FileLocationRequest(String command, FileLocationRequestArgs args) {
		super(command, args, null);
	}

}
