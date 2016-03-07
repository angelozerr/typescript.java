package ts.resources;

import java.io.IOException;

public interface ITypeScriptResourcesManagerDelegate {

	/**
	 * Return a TypeScript project associated with the specified resource. New
	 * project should be created only if it is a first such call on the
	 * resource.
	 * 
	 * @param project
	 * @param force
	 *            true if tsconfig.json project must be created if it doesn't
	 *            exists, and false otherwise.
	 * @return
	 * @throws IOException
	 */
	ITypeScriptProject getTypeScriptProject(Object project, boolean force) throws IOException;

	/**
	 * Returns true if the given file object is a TypeScript file and false
	 * otherwise.
	 * 
	 * @param fileObject
	 * @return true if the given file object is a TypeScript file and false
	 *         otherwise.
	 */
	boolean isTsFile(Object fileObject);

	/**
	 * Returns true if the given file object is a JavaScript file and false
	 * otherwise.
	 * 
	 * @param fileObject
	 * @return true if the given file object is a JavaScript file and false
	 *         otherwise.
	 */
	boolean isJsFile(Object fileObject);
}
