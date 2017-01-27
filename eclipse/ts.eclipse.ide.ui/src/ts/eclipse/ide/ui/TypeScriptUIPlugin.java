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
package ts.eclipse.ide.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.console.InstallTypesConsole;
import ts.eclipse.ide.internal.ui.console.TypeScriptConsole;
import ts.eclipse.ide.internal.ui.console.TypeScriptConsoleHelper;
import ts.eclipse.ide.ui.console.ITypeScriptConsole;

/**
 * The activator class controls the plug-in life cycle
 */
public class TypeScriptUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ts.eclipse.ide.ui"; //$NON-NLS-1$

	private static final int INTERNAL_ERROR = 10001;

	// The shared instance
	private static TypeScriptUIPlugin plugin;

	private FormToolkit fDialogsFormToolkit;

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
		try {
			if (fDialogsFormToolkit != null) {
				fDialogsFormToolkit.dispose();
				fDialogsFormToolkit = null;
			}
		} finally {
			plugin = null;
			super.stop(context);
		}
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

	public ITypeScriptConsole getInstallTypesConsole() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		InstallTypesConsole console = InstallTypesConsole.getConsole();
		TypeScriptConsoleHelper.showConsole(console);
		return console;
	}

	public FormToolkit getDialogsFormToolkit() {
		if (fDialogsFormToolkit == null) {
			FormColors colors = new FormColors(Display.getCurrent());
			colors.setBackground(null);
			colors.setForeground(null);
			fDialogsFormToolkit = new FormToolkit(colors);
		}
		return fDialogsFormToolkit;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi = new MultiStatus(PLUGIN_ID, INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, e));
	}

	public static void log(Throwable e) {
		log(TypeScriptUIMessages.TypeScriptUIPlugin_internal_error, e);
	}

}
