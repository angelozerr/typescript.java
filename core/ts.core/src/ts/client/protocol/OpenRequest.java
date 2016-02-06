package ts.client.protocol;

/**
 * Open request; value of command field is "open". Notify the server that the
 * client has file open. The server will not monitor the filesystem for changes
 * in this file and will assume that the client is updating the server (using
 * the change and/or reload messages) when the file changes. Server does not
 * currently send a response to an open request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class OpenRequest extends SimpleRequest {

	public OpenRequest(String file, String fileContent) {
		super(CommandNames.Open, new OpenRequestArgs(file, fileContent), null);
	}

}
