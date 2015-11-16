package ts.server.protocol;

/**
 * Change request message; value of command field is "change". Update the
 * server's view of the file named by argument 'file'. Server does not currently
 * send a response to a change request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ChangeRequest extends FileLocationRequest {

	public ChangeRequest(String fileName, int line, int offset, int endLine, int endOffset, String insertString) {
		super(CommandNames.Change, new ChangeRequestArgs(fileName, line, offset, endLine, endOffset, insertString));
	}

}
