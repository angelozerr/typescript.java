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
package ts.eclipse.ide.internal.core.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

import ts.client.ITypeScriptServiceClient;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.internal.core.Trace;

/**
 * TypeScript console connector manager.
 * 
 */
public class TypeScriptConsoleConnectorManager implements IRegistryChangeListener {

	private static final String EXTENSION_TYPESCRIPT_CONSOLE_CONNECTORS = "typeScriptConsoleConnectors";

	private static final TypeScriptConsoleConnectorManager INSTANCE = new TypeScriptConsoleConnectorManager();

	private List<ITypeScriptConsoleConnector> typeScriptConsoleConnectors;

	private boolean registryListenerIntialized;

	public static TypeScriptConsoleConnectorManager getManager() {
		return INSTANCE;
	}

	public TypeScriptConsoleConnectorManager() {
		this.registryListenerIntialized = false;
	}

	@Override
	public void registryChanged(final IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_CONSOLE_CONNECTORS);
		if (deltas != null) {
			for (IExtensionDelta delta : deltas)
				handleTypeScriptConsoleConnectorDelta(delta);
		}
	}

	public ITypeScriptConsoleConnector[] getTypeScriptConsoleConnectors() {
		if (typeScriptConsoleConnectors == null)
			loadTypeScriptConsoleConnectors();

		ITypeScriptConsoleConnector[] st = new ITypeScriptConsoleConnector[typeScriptConsoleConnectors.size()];
		typeScriptConsoleConnectors.toArray(st);
		return st;
	}

	/**
	 * Load the TypeScript console connectors.
	 */
	private synchronized void loadTypeScriptConsoleConnectors() {
		if (typeScriptConsoleConnectors != null)
			return;

		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .typeScriptConsoleConnectors extension point ->-");

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_CONSOLE_CONNECTORS);
		List<ITypeScriptConsoleConnector> list = new ArrayList<ITypeScriptConsoleConnector>(cf.length);
		addTypeScriptConsoleConnectors(cf, list);
		addRegistryListenerIfNeeded();
		typeScriptConsoleConnectors = list;

		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .typeScriptConsoleConnectors extension point -<-");
	}

	/**
	 * Load the TypeScript console connectors.
	 */
	private synchronized void addTypeScriptConsoleConnectors(IConfigurationElement[] cf,
			List<ITypeScriptConsoleConnector> list) {
		for (IConfigurationElement ce : cf) {
			try {
				list.add((ITypeScriptConsoleConnector) ce.createExecutableExtension("class"));
				Trace.trace(Trace.EXTENSION_POINT, "  Loaded console connectors: " + ce.getAttribute("class"));
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "  Could not load console connectors: " + ce.getAttribute("class"), t);
			}
		}
	}

	protected void handleTypeScriptConsoleConnectorDelta(IExtensionDelta delta) {
		if (typeScriptConsoleConnectors == null) // not loaded yet
			return;

		IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();

		List<ITypeScriptConsoleConnector> list = new ArrayList<ITypeScriptConsoleConnector>(
				typeScriptConsoleConnectors);
		if (delta.getKind() == IExtensionDelta.ADDED) {
			addTypeScriptConsoleConnectors(cf, list);
		} else {
			/*
			 * int size = list.size(); ITypeScriptConsoleConfiguration[] st = new
			 * ITypeScriptConsoleConfiguration[size]; list.toArray(st); int size2 =
			 * cf.length;
			 * 
			 * for (int i = 0; i < size; i++) { for (int j = 0; j < size2; j++)
			 * { if (st[i].getId().equals(cf[j].getAttribute("id"))) {
			 * st[i].dispose(); list.remove(st[i]); } } }
			 */
		}
		typeScriptConsoleConnectors = list;
	}

	private void addRegistryListenerIfNeeded() {
		if (registryListenerIntialized)
			return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, TypeScriptCorePlugin.PLUGIN_ID);
		registryListenerIntialized = true;
	}

	public void initialize() {

	}

	public void destroy() {
		if (typeScriptConsoleConnectors == null) // not loaded yet
			return;
		typeScriptConsoleConnectors.clear();
		typeScriptConsoleConnectors = null;
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

	/**
	 * Returns the TypeScript console connector to use for the given TypeScript client.
	 * 
	 * @param server
	 * @return the TypeScript console connector to use for the given TypeScript client.
	 */
	public ITypeScriptConsoleConnector getConnector(ITypeScriptServiceClient server) {
		ITypeScriptConsoleConnector[] connectors = getTypeScriptConsoleConnectors();
		for (ITypeScriptConsoleConnector connector : connectors) {
			if (connector.isAdaptFor(server)) {
				return connector;
			}
		}
		return null;
	}

}
