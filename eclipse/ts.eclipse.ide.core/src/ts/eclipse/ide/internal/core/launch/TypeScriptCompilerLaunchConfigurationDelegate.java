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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

import ts.TypeScriptException;
import ts.compiler.CompilerOptions;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.launch.TypeScriptCompilerLaunchConstants;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;

/**
 * Launch configuration delegate implementation with "tsc" to compile TypeScript
 * files *.ts to *.js/*.js.map files.
 *
 */
public class TypeScriptCompilerLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String arg1, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String buildPath = configuration.getAttribute(TypeScriptCompilerLaunchConstants.BUILD_PATH, (String) null);

		if (monitor.isCanceled()) {
			return;
		}

		IContainer container = WorkbenchResourceUtil.findContainerFromWorkspace(buildPath);
		IProject project = container.getProject();
		IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
		try {

			CompilerOptions options = new CompilerOptions();
			options.setListFiles(true);
			List<String> cmds = tsProject.getCompiler().createCommands(options, null);

			Process p = DebugPlugin.exec(cmds.toArray(new String[0]), container.getLocation().toFile(), null);
			IProcess process = null;

			Map<String, String> processAttributes = new HashMap();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, "tsc");

			if (p != null) {
				monitor.beginTask("tsc...", -1);
				process = DebugPlugin.newProcess(launch, p, buildPath, processAttributes);
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
				reporter.refreshFiles();
			}
		} catch (TypeScriptException e) {
			throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID, "Error while tsc", e));
		}
	}

}
