package ts.server.protocol;

/**
 * Arguments for completions messages.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class CompletionsRequestArgs extends FileLocationRequestArgs {

	public CompletionsRequestArgs(String fileName, int line, int offset, String prefix) {
		super(fileName, line, offset);
		if (prefix != null) {
			super.add("prefix", prefix);
		}
	}

}
