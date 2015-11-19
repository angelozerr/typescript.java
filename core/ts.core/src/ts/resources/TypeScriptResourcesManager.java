package ts.resources;

import java.io.IOException;

public class TypeScriptResourcesManager {

	private static final ConfigurableTypeScriptResourcesManager INSTANCE = ConfigurableTypeScriptResourcesManager
			.getInstance();

	/**
	 * Returns a TypeScript project object associated with the specified
	 * resource. May return null if resource doesn't point at a valid TypeScript
	 * project.
	 * 
	 * @param project
	 * @return a TypeScript project object associated with the specified
	 *         resource. May return null if resource doesn't point at a valid
	 *         TypeScript project.
	 * @throws IOException
	 */
	public static ITypeScriptProject getTypeScriptProject(Object project) {
		try {
			return INSTANCE.getTypeScriptProject(project, false);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns a TypeScript project object associated with the specified
	 * resource. May return null if resource doesn't point at a valid TypeScript
	 * project.
	 * 
	 * @param project
	 * @param force
	 *            true if .TypeScript-project must be created if it doesn't
	 *            exists, and false otherwise.
	 * 
	 * @return a TypeScript project object associated with the specified
	 *         resource. May return null if resource doesn't point at a valid
	 *         TypeScript project.
	 * @throws IOException
	 */
	public static ITypeScriptProject getTypeScriptProject(Object project, boolean force) throws IOException {
		return INSTANCE.getTypeScriptProject(project, force);
	}

	/**
	 * Returns true if the given file object is a TypeScript file and false
	 * otherwise.
	 * 
	 * @param fileObject
	 * @return true if the given file object is a TypeScript file and false
	 *         otherwise.
	 */
	public static boolean isTSFile(Object fileObject) {
		return INSTANCE.isTSFile(fileObject);
	}
}
