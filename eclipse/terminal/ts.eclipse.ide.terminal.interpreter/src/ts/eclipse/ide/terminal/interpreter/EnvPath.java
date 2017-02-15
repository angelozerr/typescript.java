package ts.eclipse.ide.terminal.interpreter;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.core.utils.Env;

public class EnvPath {

	// Reference to the monitor to lock if determining the native environment
	private final static Object ENV_GET_MONITOR = new Object();

	private static String nativeEnvironmentPathCasePreserved = null;

	public static void insertToEnvPath(Map<String, Object> properties, String... vars) {
		String newEnvPath = insertToEnvPath(vars);
		properties.put(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT, new String[] { newEnvPath });
	}

	public static String insertToEnvPath(String... vars) {
		String nativeEnvPath = getNativeEnvironmentPathCasePreserved();
		StringBuilder envPath = new StringBuilder(nativeEnvPath.substring(0, "PATH=".length()));
		boolean empty = true;
		for (String var : vars) {
			if (var == null) {
				continue;
			}
			if (!empty) {
				envPath.append(getSeparator());
			}
			envPath.append(var);
			empty = false;
		}
		envPath.append(getSeparator());
		envPath.append(nativeEnvPath.substring("PATH=".length(), nativeEnvPath.length()));
		return envPath.toString();
	}

	private static String getSeparator() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return ";";
		}
		return ":";
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
				if (envVar.toUpperCase().startsWith("PATH=")) {
					nativeEnvironmentPathCasePreserved = envVar;
					return nativeEnvironmentPathCasePreserved;
				}
			}
			nativeEnvironmentPathCasePreserved = "Path=";
			return nativeEnvironmentPathCasePreserved;
		}
	}
}
