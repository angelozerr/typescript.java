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
 * Format request; value of command field is "format". Return response giving
 * zero or more edit instructions. The edit instructions will be sorted in file
 * order. Applying the edit instructions in reverse to file will result in
 * correctly reformatted text.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FormatRequest extends FileLocationRequest {

	public FormatRequest(String fileName, int line, int offset, int endLine, int endOffset) {
		super(CommandNames.Format, new FormatRequestArgs(fileName, line, offset, endLine, endOffset));
	}

}
