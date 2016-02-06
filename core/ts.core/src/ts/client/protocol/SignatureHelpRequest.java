package ts.client.protocol;

/**
 * Signature help request; value of command field is "signatureHelp". Given a
 * file location (file, line, col), return the signature help.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class SignatureHelpRequest extends FileLocationRequest {

	public SignatureHelpRequest(String fileName, int line, int offset) {
		super(CommandNames.SignatureHelp, new SignatureHelpRequestArgs(fileName, line, offset));
	}
}
