package org.eclipse.jface.text.provisional.codelens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CodeLensProviderRegistry {

	private static final CodeLensProviderRegistry INSTANCE = new CodeLensProviderRegistry();
	
	public static CodeLensProviderRegistry getInstance() {
		return INSTANCE;
	}
	
	private final Map<String, Collection<ICodeLensProvider>> providersMap;

	public CodeLensProviderRegistry() {
		this.providersMap = new HashMap<>();
	}

	public void register(String target, ICodeLensProvider provider) {
		Collection<ICodeLensProvider> providers = providersMap.get(target);
		if (providers == null) {
			providers = new ArrayList<>();
			providersMap.put(target, providers);
		}
		providers.add(provider);
	}

	public Collection<ICodeLensProvider> all(String target) {
		return providersMap.get(target);
	}

}
