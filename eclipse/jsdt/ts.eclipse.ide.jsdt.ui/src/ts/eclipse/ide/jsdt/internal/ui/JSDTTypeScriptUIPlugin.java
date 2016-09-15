/**
 *  Copyright (c) 2015-2016 Angelo ZERR and Genuitec LLC.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Piotr Tomiak <piotr@genuitec.com> - unified completion proposals calculation
 */
package ts.eclipse.ide.jsdt.internal.ui;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;
import org.osgi.framework.BundleContext;

import ts.eclipse.ide.jsdt.core.JSDTTypeScriptCorePlugin;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptDocumentProvider;
import ts.eclipse.ide.jsdt.internal.ui.text.TypeScriptTextTools;

/**
 * The activator class controls the plug-in life cycle
 */
public class JSDTTypeScriptUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ts.eclipse.ide.jsdt.ui"; //$NON-NLS-1$

	private static final int INTERNAL_ERROR = 10001;

	// The shared instance
	private static JSDTTypeScriptUIPlugin plugin;

	private TypeScriptDocumentProvider documentProvider;

	private TypeScriptTextTools fJavaTextTools;

	/**
	 * The key to store customized templates.
	 * 
	 */
	private static final String TEMPLATES_KEY = "ts.eclipse.ide.jsdt.ui.text.custom_templates"; //$NON-NLS-1$

	private static final String CONTEXT_TYPE_REGISTRY_ID = "ts.eclipse.ide.jsdt.ui.ContextTypeRegistry";

	/**
	 * The template context type registry for the java editor.
	 * 
	 */
	private ContextTypeRegistry fContextTypeRegistry;

	/**
	 * The template store for the java editor.
	 * 
	 */
	private TemplateStore fTemplateStore;

	/**
	 * The combined preference store.
	 * 
	 */
	private IPreferenceStore fCombinedPreferenceStore;

	/**
	 * The constructor
	 */
	public JSDTTypeScriptUIPlugin() {
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
	public static JSDTTypeScriptUIPlugin getDefault() {
		return plugin;
	}

	public synchronized IDocumentProvider getTypeScriptDocumentProvider() {
		if (documentProvider == null) {
			documentProvider = new TypeScriptDocumentProvider();
		}
		return documentProvider;
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

	public synchronized TypeScriptTextTools getJavaTextTools() {
		if (fJavaTextTools == null)
			fJavaTextTools = new TypeScriptTextTools(getPreferenceStore(),
					JavaScriptCore.getPlugin().getPluginPreferences());
		return fJavaTextTools;
	}

	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, e));
	}

	public static void log(Throwable e) {
		log("", e);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Returns the template context type registry for the java plug-in.
	 * 
	 * @return the template context type registry for the java plug-in
	 * 
	 */
	public ContextTypeRegistry getTemplateContextRegistry() {
		if (fContextTypeRegistry == null) {
			ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry(CONTEXT_TYPE_REGISTRY_ID);
			fContextTypeRegistry = registry;
		}

		return fContextTypeRegistry;
	}

	/**
	 * Returns the template store for the java editor templates.
	 * 
	 * @return the template store for the java editor templates
	 * 
	 */
	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			final IPreferenceStore store = getPreferenceStore();
			fTemplateStore = new ContributionTemplateStore(getTemplateContextRegistry(), store, TEMPLATES_KEY);
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
			fTemplateStore.startListeningForPreferenceChanges();
		}

		return fTemplateStore;
	}

	/**
	 * Returns a combined preference store, this store is read-only.
	 * 
	 * @return the combined preference store
	 * 
	 * 
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		if (fCombinedPreferenceStore == null) {
			IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
			fCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { getPreferenceStore(),
					new PreferencesAdapter(JSDTTypeScriptCorePlugin.getDefault().getPluginPreferences()),
					generalTextStore });
		}
		return fCombinedPreferenceStore;
	}

	/**
	 * Returns a section in the Java plugin's dialog settings. If the section doesn't exist yet, it is created.
	 *
	 * @param name the name of the section
	 * @return the section of the given name
	 * @since 3.2
	 */
	public IDialogSettings getDialogSettingsSection(String name) {
		IDialogSettings dialogSettings= getDialogSettings();
		IDialogSettings section= dialogSettings.getSection(name);
		if (section == null) {
			section= dialogSettings.addNewSection(name);
		}
		return section;
	}
}
