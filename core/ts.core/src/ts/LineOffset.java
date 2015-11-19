package ts;

public class LineOffset {

	private final int line;
	private final int offset;

	public LineOffset(int line, int offset) {
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
