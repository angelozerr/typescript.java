package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import ts.TypeScriptException;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.AbstractTypeScriptSettings;
import ts.eclipse.ide.internal.core.Trace;

class IDEResourcesProjectStatus extends AbstractTypeScriptSettings {

	private boolean typeScript;
	private boolean salsa;
	private IDETypeScriptProject tsProject;

	public IDEResourcesProjectStatus(IProject project) {
		super(project, TypeScriptCorePlugin.PLUGIN_ID);
		setTypeScript(
				isMatchPaths(super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NATURE_TYPESCRIPT_PATHS,
						TypeScriptCorePreferenceConstants.DEFAULT_NATURE_TYPESCRIPT_PATHS)));
		setSalsa(isMatchPaths(super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NATURE_SALSA_PATHS,
				TypeScriptCorePreferenceConstants.DEFAULT_NATURE_SALSA_PATHS)));
		update();
	}

	private void update() {
		if (tsProject != null) {
			try {
				tsProject.dispose();
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while disposing TypeScript project", e);
			}
		}
		if (isSalsa() || isTypeScript()) {
			this.tsProject = new IDETypeScriptProject(getProject());
		} else {
			this.tsProject = null;
		}
	}

	boolean isTypeScript() {
		return typeScript;
	}

	void setTypeScript(boolean typeScript) {
		this.typeScript = typeScript;
	}

	boolean isSalsa() {
		return salsa;
	}

	void setSalsa(boolean salsa) {
		this.salsa = salsa;
	}

	IDETypeScriptProject getTypeScriptProject() {
		return tsProject;
	}

	void setTypeScriptProject(IDETypeScriptProject tsProject) {
		this.tsProject = tsProject;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (TypeScriptCorePreferenceConstants.NATURE_TYPESCRIPT_PATHS.equals(event.getKey())) {
			setTypeScript(isMatchPaths(event.getNewValue().toString()));
		} else if (TypeScriptCorePreferenceConstants.NATURE_SALSA_PATHS.equals(event.getKey())) {
			setSalsa(isMatchPaths(event.getNewValue().toString()));
		}
	}

	private boolean isMatchPaths(String paths) {
		String[] p = paths.split(",");
		for (int i = 0; i < p.length; i++) {
			if (getProject().getFile(p[i]).exists()) {
				return true;
			}
		}
		return false;
	}

}