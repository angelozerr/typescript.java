package ts.client.protocol;

/**
 * Request whose sole parameter is a file name.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileRequest extends SimpleRequest {

	public FileRequest(CommandNames command, FileRequestArgs args, Integer seq) {
		super(command, args, seq);
	}

	public FileRequest(String command, FileRequestArgs args, Integer seq) {
		super(command, args, seq);
	}

}
