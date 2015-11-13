package ts.server.protocol;

/**
 * Completions request; value of command field is "completions". Given a file
 * location (file, line, col) and a prefix (which may be the empty string),
 * return the possible completions that begin with prefix.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class CompletionsRequest extends FileLocationRequest {

	public CompletionsRequest(String fileName, int line, int offset, String prefix, ISequenceProvider provider) {
		super(CommandNames.Completions, new CompletionsRequestArgs(fileName, line, offset, prefix), provider);
	}

}
