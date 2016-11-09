package ts.internal.client.protocol;

/**
 * Arguments for CompileOnSaveEmitFileRequest
 */
public class CompileOnSaveEmitFileRequestArgs extends FileRequestArgs {

	/**
	 * 
	 * @param file
	 * @param forced
	 *            if true - then file should be recompiled even if it does not
	 *            have any changes.
	 */
	public CompileOnSaveEmitFileRequestArgs(String file, Boolean forced) {
		super(file);
		if (forced != null) {
			super.add("forced", forced);
		}
	}

}
