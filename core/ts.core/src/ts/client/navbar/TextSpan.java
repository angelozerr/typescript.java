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
package ts.client.navbar;

import ts.TypeScriptException;
import ts.client.Location;

public class TextSpan {

	private Location start;
	private Location end;
	NavigationBarItem parent;

	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

	public boolean contains(int position) throws TypeScriptException {
		int positionStart = start.getPosition();
		return positionStart <= position && position < (positionStart + getLength());
	}

	public int getLength() throws TypeScriptException {
		int positionStart = start.getPosition();
		int positionEnd = end.getPosition();
		return positionEnd - positionStart;
	}

	public NavigationBarItem getParent() {
		return parent;
	}
}
