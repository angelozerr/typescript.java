package ts.client.protocol;

/**
 * A request whose arguments specify a file location (file, line, col).
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FileLocationRequest extends FileRequest {

	public FileLocationRequest(CommandNames command, FileLocationRequestArgs args) {
		super(command, args, null);
	}

	public FileLocationRequest(String command, FileLocationRequestArgs args) {
		super(command, args, null);
	}

}
