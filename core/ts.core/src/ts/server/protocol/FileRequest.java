package ts.server.protocol;

/**
 * Request whose sole parameter is a file name.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileRequest extends Request {

	public FileRequest(CommandNames command, FileRequestArgs args, ISequenceProvider provider) {
		super(command, args, provider);
	}

	public FileRequest(String command, FileRequestArgs args, ISequenceProvider provider) {
		super(command, args, provider);
	}

}
