/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.console.TypeScriptConsole;
import ts.eclipse.ide.internal.ui.console.TypeScriptConsoleHelper;
import ts.eclipse.ide.ui.console.ITypeScriptConsole;

/**
 * The activator class controls the plug-in life cycle
 */
public class TypeScriptUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ts.eclipse.ide.ui"; //$NON-NLS-1$

	// The shared instance
	private static TypeScriptUIPlugin plugin;

	/**
	 * The constructor
	 */
	public TypeScriptUIPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TypeScriptUIPlugin getDefault() {
		return plugin;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * @return Returns the active workbench window's currrent page.
	 */
	public static IWorkbenchPage getActivePage() {
		return getActiveWorkbenchWindow().getActivePage();
	}

	public ITypeScriptConsole getConsole(IIDETypeScriptProject project) {
		if (project.isServerDisposed()) {
			return null;
		}
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		TypeScriptConsole console = TypeScriptConsole.getOrCreateConsole(project);
		TypeScriptConsoleHelper.showConsole(console);
		return console;
	}

}
