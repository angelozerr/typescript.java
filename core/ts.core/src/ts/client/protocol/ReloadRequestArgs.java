package ts.client.protocol;

/**
 * Arguments for reload request.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class ReloadRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param fileName
	 * @param tmpfile
	 *            Name of temporary file from which to reload file contents. May
	 *            be same as file.
	 */
	public ReloadRequestArgs(String fileName, String tmpfile) {
		super(fileName);
		super.add("tmpfile", tmpfile);
	}

}
