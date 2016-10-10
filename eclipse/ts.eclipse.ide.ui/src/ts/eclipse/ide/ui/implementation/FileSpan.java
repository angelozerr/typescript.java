package ts.eclipse.ide.ui.implementation;

public class FileSpan {

	private final String file;
	private final int startLine;
	private final int startOffset;
	private final int endLine;
	private final int endOffset;

	public FileSpan(String file, int startLine, int startOffset, int endLine, int endOffset) {
		this.file = file;
		this.startLine = startLine;
		this.startOffset = startOffset;
		this.endLine = endLine;
		this.endOffset = endOffset;
	}

	public String getFile() {
		return file;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartOffset() {
		return startOffset;
	}
	
	public int getEndLine() {
		return endLine;
	}

	public int getEndOffset() {
		return endOffset;
	}

}
