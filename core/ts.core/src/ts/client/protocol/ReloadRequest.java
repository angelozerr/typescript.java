package ts.client.protocol;

/**
 * Reload request message; value of command field is "reload". Reload contents
 * of file with name given by the 'file' argument from temporary file with name
 * given by the 'tmpfile' argument. The two names can be identical.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ReloadRequest extends FileRequest {

	public ReloadRequest(String fileName, String tmpfile, int seq) {
		super(CommandNames.Reload, new ReloadRequestArgs(fileName, tmpfile), seq);
	}

}
