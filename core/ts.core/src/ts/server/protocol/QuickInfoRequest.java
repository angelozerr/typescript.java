package ts.server.protocol;

/**
 *
 * Quickinfo request; value of command field is "quickinfo". Return response
 * giving a quick type and documentation string for the symbol found in file at
 * location line, col.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class QuickInfoRequest extends FileLocationRequest {

	public QuickInfoRequest(String fileName, int line, int offset) {
		super(CommandNames.QuickInfo, new FileLocationRequestArgs(fileName, line, offset));
	}
}
