package org.eclipse.jface.text.provisional.codelens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.codelens.internal.CodeLensPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

public class CodeLensProviderRegistry implements IRegistryChangeListener {

	private static final CodeLensProviderRegistry INSTANCE = new CodeLensProviderRegistry();
	private static final String EXTENSION_CODELENS_PROVIDERS = "codeLensProviders";

	public static CodeLensProviderRegistry getInstance() {
		return INSTANCE;
	}

	private boolean codeLensProviderLoaded;
	private final Map<String, List<ICodeLensProvider>> providersMap;

	public CodeLensProviderRegistry() {
		this.providersMap = new HashMap<>();
		this.codeLensProviderLoaded = false;
	}

	public void register(String target, ICodeLensProvider provider) {
		List<ICodeLensProvider> providers = providersMap.get(target);
		if (providers == null) {
			providers = new ArrayList<>();
			providersMap.put(target, providers);
		}
		providers.add(provider);
	}

	public List<ICodeLensProvider> all(String target) {
		loadCodeLensProvidersIfNeeded();
		return providersMap.get(target);
	}

	@Override
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(CodeLensPlugin.PLUGIN_ID, EXTENSION_CODELENS_PROVIDERS);
		if (deltas != null) {
			for (IExtensionDelta delta : deltas)
				handleCodeLensProvidersDelta(delta);
		}
	}

	private void loadCodeLensProvidersIfNeeded() {
		if (codeLensProviderLoaded) {
			return;
		}
		loadCodeLensProviders();
	}

	/**
	 * Load the SourceMap language supports.
	 */
	private synchronized void loadCodeLensProviders() {
		if (codeLensProviderLoaded) {
			return;
		}

		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			if (registry == null) {
				return;
			}
			IConfigurationElement[] cf = registry.getConfigurationElementsFor(CodeLensPlugin.PLUGIN_ID,
					EXTENSION_CODELENS_PROVIDERS);
			loadCodeLensProvidersFromExtension(cf);
		} finally {
			codeLensProviderLoaded = true;
		}
	}

	/**
	 * Add the SourceMap language supports.
	 */
	private synchronized void loadCodeLensProvidersFromExtension(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				String target = ce.getAttribute("target");
				ICodeLensProvider provider = (ICodeLensProvider) ce.createExecutableExtension("class");
				register(target, provider);
			} catch (Throwable e) {
				CodeLensPlugin.log(e);
			}
		}
	}

	protected void handleCodeLensProvidersDelta(IExtensionDelta delta) {
		if (!codeLensProviderLoaded) // not loaded yet
			return;

		IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();

		// List<CodeLensProviderType> list = new
		// ArrayList<CodeLensProviderType>(
		// codeLensProviders);
		// if (delta.getKind() == IExtensionDelta.ADDED) {
		// loadCodeLensProvidersFromExtension(cf, list);
		// } else {
		// int size = list.size();
		// CodeLensProviderType[] st = new CodeLensProviderType[size];
		// list.toArray(st);
		// int size2 = cf.length;
		//
		// for (int i = 0; i < size; i++) {
		// for (int j = 0; j < size2; j++) {
		// if (st[i].getId().equals(cf[j].getAttribute("id"))) {
		// list.remove(st[i]);
		// }
		// }
		// }
		// }
		// codeLensProviders = list;
	}

	public void initialize() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, CodeLensPlugin.PLUGIN_ID);
	}

	public void destroy() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}
}
