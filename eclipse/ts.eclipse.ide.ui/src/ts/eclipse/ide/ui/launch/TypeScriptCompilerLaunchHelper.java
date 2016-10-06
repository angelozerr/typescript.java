package ts.eclipse.ide.ui.launch;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.launch.TypeScriptCompilerLaunchConstants;

public class TypeScriptCompilerLaunchHelper {

	public static void launch(IFile tsconfigFile) {
		launch(tsconfigFile, ILaunchManager.RUN_MODE);
	}

	public static void launch(IFile tsconfigFile, String mode) {
		launch(tsconfigFile.getParent(), mode);
	}

	public static void launch(IContainer buildPath, String mode) {
		ILaunchConfigurationType tscLaunchConfigurationType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(TypeScriptCompilerLaunchConstants.LAUNCH_CONFIGURATION_ID);
		try {
			// Check if configuration already exists
			ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurations(tscLaunchConfigurationType);

			ILaunchConfiguration existingConfiguraion = chooseLaunchConfiguration(configurations, buildPath,
					TypeScriptCompilerLaunchConstants.BUILD_PATH);

			if (existingConfiguraion != null) {
				ILaunchConfigurationWorkingCopy wc = existingConfiguraion.getWorkingCopy();
				// Updating task in the existing launch
				// wc.setAttribute(GulpConstants.COMMAND, task.getName());
				existingConfiguraion = wc.doSave();
				DebugUITools.launch(existingConfiguraion, mode);
				// Creating Launch Configuration from scratch
			} else if (buildPath != null) {
				IProject project = buildPath.getProject();
				ILaunchConfigurationWorkingCopy newConfiguration = createEmptyLaunchConfiguration(
						project.getName() + " [" + buildPath.getProjectRelativePath().toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				newConfiguration.setAttribute(TypeScriptCompilerLaunchConstants.BUILD_PATH,
						buildPath.getLocation().toOSString());
				newConfiguration.setAttribute(TypeScriptCompilerLaunchConstants.PROJECT, project.getName());
				newConfiguration.doSave();
				DebugUITools.launch(newConfiguration, mode);
			}

		} catch (CoreException e) {
			TypeScriptCorePlugin.logError(e, e.getMessage());
		}

	}

	private static ILaunchConfigurationWorkingCopy createEmptyLaunchConfiguration(String namePrefix)
			throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigurationType = launchManager
				.getLaunchConfigurationType(TypeScriptCompilerLaunchConstants.LAUNCH_CONFIGURATION_ID);
		ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null,
				launchManager.generateLaunchConfigurationName(namePrefix));
		return launchConfiguration;
	}

	private static ILaunchConfiguration chooseLaunchConfiguration(ILaunchConfiguration[] configurations,
			IContainer container, String attribute) {
		try {
			for (ILaunchConfiguration conf : configurations) {
				String buildFileAttribute = conf.getAttribute(attribute, (String) null);
				String buildFilePath = container.getLocation().toOSString();
				// Launch Configuration per build file (i.e. Gruntfile.js /
				// gulpfile.js)
				if (buildFilePath.equals(buildFileAttribute)) {
					return conf;
				}
			}
		} catch (CoreException e) {
			TypeScriptCorePlugin.logError(e, e.getMessage());
		}
		return null;
	}

}
