package ts.server.protocol;

/**
 * Instances of this interface specify a location in a source file: (file, line,
 * character offset), where line and character offset are 1-based.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileLocationRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param line
	 *            The line number for the request (1-based).
	 * @param offset
	 *            The character offset (on the line) for the request (1-based).
	 */
	public FileLocationRequestArgs(String fileName, int line, int offset) {
		super(fileName);
		super.add("line", line);
		super.add("offset", offset);
	}

}
