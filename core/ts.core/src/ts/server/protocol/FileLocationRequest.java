package ts.server.protocol;

/**
 * A request whose arguments specify a file location (file, line, col).
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileLocationRequest extends FileRequest {

	public FileLocationRequest(CommandNames command, FileLocationRequestArgs args, ISequenceProvider provider) {
		super(command, args, provider);
	}

	public FileLocationRequest(String command, FileLocationRequestArgs args, ISequenceProvider provider) {
		super(command, args, provider);
	}

}
