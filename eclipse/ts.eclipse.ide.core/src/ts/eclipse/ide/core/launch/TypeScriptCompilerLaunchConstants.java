package ts.eclipse.ide.core.launch;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;

import ts.eclipse.ide.core.TypeScriptCorePlugin;

public class TypeScriptCompilerLaunchConstants {

	// Launch constants
	public static final String LAUNCH_CONFIGURATION_ID = TypeScriptCorePlugin.PLUGIN_ID + ".tscLaunchConfigurationType"; //$NON-NLS-1$
	public static final String BUILD_PATH = IExternalToolConstants.ATTR_WORKING_DIRECTORY;

}
