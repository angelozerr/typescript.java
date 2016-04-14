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
 * Get occurrences request; value of command field is "occurrences". Return
 * response giving spans that are relevant in the file at a given line and
 * column.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class OccurrencesRequest extends FileLocationRequest {

	public OccurrencesRequest(String fileName, int line, int offset) {
		super(CommandNames.Occurrences, new FileLocationRequestArgs(fileName, line, offset));
	}

}
