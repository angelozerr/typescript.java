package ts.server.protocol;

/**
 * Arguments of a signature help request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class SignatureHelpRequestArgs extends FileLocationRequestArgs {

	public SignatureHelpRequestArgs(String fileName, int line, int offset) {
		super(fileName, line, offset);
	}

}
