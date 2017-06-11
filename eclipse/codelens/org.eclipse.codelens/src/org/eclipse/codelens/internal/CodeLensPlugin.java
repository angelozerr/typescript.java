/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - getter for ProblemManager
 */
package org.eclipse.codelens.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.provisional.codelens.CodeLensProviderRegistry;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodeLensPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.codelens"; //$NON-NLS-1$

	// The shared instance.
	private static CodeLensPlugin plugin;

	/**
	 * The constructor.
	 */
	public CodeLensPlugin() {
		super();
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		CodeLensProviderRegistry.getInstance().initialize();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		CodeLensProviderRegistry.getInstance().initialize();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CodeLensPlugin getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		CodeLensPlugin plugin = getDefault();
		if (plugin != null) {
			plugin.getLog().log(status);
		} else {
			System.err.println(status.getPlugin() + ": " + status.getMessage()); //$NON-NLS-1$
		}
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			log(new Status(IStatus.ERROR, PLUGIN_ID, ((CoreException) e).getStatus().getSeverity(), e.getMessage(),
					e.getCause()));
		} else {
			log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
		}
	}
}
