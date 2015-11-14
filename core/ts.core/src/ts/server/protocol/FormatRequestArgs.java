package ts.server.protocol;

/**
 * Arguments for format messages.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FormatRequestArgs extends FileLocationRequestArgs {

	public FormatRequestArgs(String fileName, int line, int offset, int endLine, int endOffset) {
		super(fileName, line, offset);
		super.add("endLine", endLine);
		super.add("endOffset", endOffset);
	}

}
