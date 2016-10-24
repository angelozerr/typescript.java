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
package ts.eclipse.ide.internal.core.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;

import ts.TypeScriptException;
import ts.cmd.tsc.CompilerOptions;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.launch.TypeScriptCompilerLaunchConstants;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.TypeScriptCoreMessages;

/**
 * Launch configuration delegate implementation with "tsc" to compile TypeScript
 * files *.ts to *.js/*.js.map files.
 *
 */
public class TypeScriptCompilerLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String arg1, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IPath buildPath = getBuildPath(configuration);

		if (monitor.isCanceled()) {
			return;
		}

		IFile tsconfigFile = WorkbenchResourceUtil.findFileFromWorkspace(buildPath);
		IContainer container = tsconfigFile.getParent();
		IProject project = tsconfigFile.getProject();
		IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
		try {

			CompilerOptions options = new CompilerOptions();
			options.setListFiles(true);
			List<String> tsconfigArgs = new ArrayList<String>();
			tsconfigArgs.add("--p");
			tsconfigArgs.add(tsconfigFile.getName());
			List<String> cmds = tsProject.getCompiler().createCommands(options, tsconfigArgs);

			Process p = DebugPlugin.exec(cmds.toArray(new String[0]), container.getLocation().toFile(), null);
			IProcess process = null;

			Map<String, String> processAttributes = new HashMap<String, String>();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, "tsc");

			if (p != null) {
				monitor.beginTask("tsc...", -1);
				process = DebugPlugin.newProcess(launch, p, buildPath.toString(), processAttributes);
			}

			TscStreamListener reporter = new TscStreamListener(container);
			process.getStreamsProxy().getOutputStreamMonitor().addListener(reporter);

			if (!reporter.isWatch()) {
				while (!process.isTerminated()) {
					try {
						if (monitor.isCanceled()) {
							process.terminate();
							break;
						}
						Thread.sleep(50L);
					} catch (InterruptedException localInterruptedException) {
					}
				}
				reporter.onCompilationCompleteWatchingForFileChanges();
			}
		} catch (TypeScriptException e) {
			throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID, "Error while tsc", e));
		}
	}

	/**
	 * Expands and returns the working directory attribute of the given launch
	 * configuration. Returns <code>null</code> if a working directory is not
	 * specified. If specified, the working is verified to point to an existing
	 * tsconfig.json file in the local file system.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return an absolute path to a directory in the local file system, or
	 *         <code>null</code> if unspecified
	 * @throws CoreException
	 *             if unable to retrieve the associated launch configuration
	 *             attribute, if unable to resolve any variables, or if the
	 *             resolved location does not point to an existing tsconfig.json
	 *             file in the local file system.
	 */
	private static IPath getBuildPath(ILaunchConfiguration configuration) throws CoreException {
		String location = configuration.getAttribute(TypeScriptCompilerLaunchConstants.BUILD_PATH, (String) null);
		if (location != null) {
			String expandedLocation = getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation.length() > 0) {
				File path = new File(expandedLocation);
				if (path.isFile()) {
					return new Path(expandedLocation);
				}
				String msg = NLS.bind(
						TypeScriptCoreMessages.TypeScriptCompilerLaunchConfigurationDelegate_invalidBuildPath,
						new Object[] { expandedLocation, configuration.getName() });
				abort(msg, null, 0);
			}
		}
		return null;
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	private static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID, code, message, exception));
	}

	private static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}
}
