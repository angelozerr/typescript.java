package ts.client.codefixes;

import ts.client.Location;

public class CodeEdit {

	/**
	 * First character of the text span to edit.
	 */
	private Location start;

	/**
	 * One character past last character of the text span to edit.
	 */
	private Location end;

	/**
	 * Replace the span defined above with this string (may be the empty
	 * string).
	 */
	private String newText;

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		return end;
	}

	public String getNewText() {
		return newText;
	}
}
