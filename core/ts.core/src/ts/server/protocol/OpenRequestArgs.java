package ts.server.protocol;

/**
 * Information found in an "open" request.
 */
public class OpenRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param file
	 * @param fileContent
	 *            Used when a version of the file content is known to be more up
	 *            to date than the one on disk. Then the known content will be
	 *            used upon opening instead of the disk copy
	 */
	public OpenRequestArgs(String file, String fileContent) {
		super(file);
		if (fileContent != null) {
			super.add("fileContent", fileContent);
		}
	}

}
