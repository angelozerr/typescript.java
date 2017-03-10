package ts.eclipse.ide.terminal.interpreter;

import java.io.File;
import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.core.utils.Env;

public class EnvPath {

	private static final String PATH_ENV = "PATH=";

	// Reference to the monitor to lock if determining the native environment
	private final static Object ENV_GET_MONITOR = new Object();

	private static String nativeEnvironmentPathCasePreserved = null;

	public static void insertToEnvPath(Map<String, Object> properties, String... vars) {
		String newEnvPath = insertToEnvPath(vars);
		properties.put(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT, new String[] { newEnvPath });
	}

	public static String insertToEnvPath(String... vars) {
		String nativeEnvPath = getNativeEnvironmentPathCasePreserved();
		StringBuilder envPath = new StringBuilder(nativeEnvPath.substring(0, PATH_ENV.length()));
		boolean empty = true;
		for (String var : vars) {
			if (var == null) {
				continue;
			}
			if (!empty) {
				envPath.append(File.pathSeparatorChar);
			}
			envPath.append(var);
			empty = false;
		}
		envPath.append(File.pathSeparatorChar);
		envPath.append(nativeEnvPath.substring(PATH_ENV.length(), nativeEnvPath.length()));
		return envPath.toString();
	}

	/**
	 * Determine the native environment.
	 *
	 * @return The native environment, or an empty map.
	 */
	private static String getNativeEnvironmentPathCasePreserved() {
		synchronized (ENV_GET_MONITOR) {
			if (nativeEnvironmentPathCasePreserved != null) {
				return nativeEnvironmentPathCasePreserved;
			}
			String[] envVars = Env.getEnvironment(null, false);
			for (String envVar : envVars) {
				if (envVar.toUpperCase().startsWith(PATH_ENV)) {
					nativeEnvironmentPathCasePreserved = envVar;
					return nativeEnvironmentPathCasePreserved;
				}
			}
			nativeEnvironmentPathCasePreserved = PATH_ENV;
			return nativeEnvironmentPathCasePreserved;
		}
	}
}
