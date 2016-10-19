package ts.internal.client.protocol;

public class CodeFixRequestArgs extends FileRequestArgs {

	public CodeFixRequestArgs(String file, int startLine, int startOffset, int endLine, int endOffset) {
		super(file);
		super.add("startLine", startLine);
		super.add("startOffset", startOffset);
		super.add("endLine", endLine);
		super.add("endOffset", endOffset);
	}

}
