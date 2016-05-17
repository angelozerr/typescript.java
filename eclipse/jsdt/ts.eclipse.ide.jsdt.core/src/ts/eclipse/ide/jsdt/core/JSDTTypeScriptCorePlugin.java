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
package ts.eclipse.ide.jsdt.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JSDTTypeScriptCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ts.eclipse.ide.jsdt.core"; //$NON-NLS-1$

	// The shared instance
	private static JSDTTypeScriptCorePlugin plugin;

	private boolean isJSDT2;

	/**
	 * The constructor
	 */
	public JSDTTypeScriptCorePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		//Platform.getPlugin("org.eclipse.wst.jsdt.core")
		isJSDT2 = Platform.getBundle("org.eclipse.wst.jsdt.core").getVersion().toString().startsWith("2.0.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JSDTTypeScriptCorePlugin getDefault() {
		return plugin;
	}

	public boolean isJSDT2() {		
		return isJSDT2;
	}

}
