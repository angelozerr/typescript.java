package ts.eclipse.ide.core.resources;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;

public class WorkspaceTypeScriptSettingsHelper {

	private static UseSalsa useSalsa;

	static {
		IEclipsePreferences preferences = WorkspaceTypeScriptSettingsHelper
				.getWorkspacePreferences(TypeScriptCorePlugin.PLUGIN_ID);
		String name = preferences.get(TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE,
				UseSalsa.WhenNoJSDTNature.name());
		useSalsa = UseSalsa.valueOf(name);
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {

			public void preferenceChange(PreferenceChangeEvent event) {
				if (TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE.equals(event.getKey())) {
					try {
						useSalsa = UseSalsa.valueOf(event.getNewValue().toString());
					} catch (Throwable e) {

					}
				}
			}
		});
	}

	public static IEclipsePreferences getWorkspacePreferences(String pluginId) {
		return DefaultScope.INSTANCE.getNode(pluginId);
	}

	public static UseSalsa getUseSalsa() {
		if (useSalsa == null) {
			return UseSalsa.WhenNoJSDTNature;
		}
		return useSalsa;
	}

}
