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
package ts.client;

/**
 * Bean for location line/offset used by tsserver.
 *
 */
public class Location {

	private final int line;
	private final int offset;

	public Location(int line, int offset) {
		this.line = line;
		this.offset = offset;
	}

	/**
	 * Returns the line location.
	 * 
	 * @return the line location.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the offset location.
	 * 
	 * @return the offset location.
	 */
	public int getOffset() {
		return offset;
	}
}
