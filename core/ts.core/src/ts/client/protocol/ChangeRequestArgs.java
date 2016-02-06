package ts.client.protocol;

/**
 * Arguments for change request message.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ChangeRequestArgs extends FormatRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param line
	 * @param offset
	 * @param endLine
	 * @param endOffset
	 * @param insertString
	 *            Optional string to insert at location (file, line, offset).
	 */
	public ChangeRequestArgs(String fileName, int line, int offset, int endLine, int endOffset, String insertString) {
		super(fileName, line, offset, endLine, endOffset);
		if (insertString != null) {
			super.add("insertString", insertString);
		}
	}

}
