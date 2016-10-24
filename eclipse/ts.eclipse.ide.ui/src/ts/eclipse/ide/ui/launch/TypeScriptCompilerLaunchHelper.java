/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.launch.TypeScriptCompilerLaunchConstants;

/**
 * tsc launch helper.
 *
 */
public class TypeScriptCompilerLaunchHelper {

	public static String getBuildPath(IResource resource) {
		return new StringBuilder("${workspace_loc:/").append(resource.getProject().getName()).append("/")
				.append(resource.getProjectRelativePath()).append("}").toString();
	}

	public static void launch(IFile tsconfigFile) {
		launch(tsconfigFile, ILaunchManager.RUN_MODE);
	}

	public static void launch(IFile tsconfigFile, String mode) {
		ILaunchConfigurationType tscLaunchConfigurationType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(TypeScriptCompilerLaunchConstants.LAUNCH_CONFIGURATION_ID);
		try {
			// Check if configuration already exists
			ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurations(tscLaunchConfigurationType);

			ILaunchConfiguration existingConfiguraion = chooseLaunchConfiguration(configurations, tsconfigFile);

			if (existingConfiguraion != null) {
				ILaunchConfigurationWorkingCopy wc = existingConfiguraion.getWorkingCopy();
				existingConfiguraion = wc.doSave();
				DebugUITools.launch(existingConfiguraion, mode);
				// Creating Launch Configuration from scratch
			} else {
				IProject project = tsconfigFile.getProject();
				ILaunchConfigurationWorkingCopy newConfiguration = createEmptyLaunchConfiguration(
						project.getName() + " [" + tsconfigFile.getProjectRelativePath().toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				newConfiguration.setAttribute(TypeScriptCompilerLaunchConstants.BUILD_PATH, getBuildPath(tsconfigFile));
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
			IFile tsconfigFile) {
		String buildFilePath = getBuildPath(tsconfigFile);
		try {
			for (ILaunchConfiguration conf : configurations) {
				String buildFileAttribute = conf.getAttribute(TypeScriptCompilerLaunchConstants.BUILD_PATH,
						(String) null);
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
