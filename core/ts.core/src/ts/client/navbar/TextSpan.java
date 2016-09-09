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
