package ts;

public class Location {

	private final int line;
	private final int offset;

	public Location(int line, int offset) {
		this.line = line;
		this.offset = offset;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}
}
