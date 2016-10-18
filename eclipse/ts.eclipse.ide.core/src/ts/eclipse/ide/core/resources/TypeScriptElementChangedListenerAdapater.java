package ts.eclipse.ide.core.resources;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;

public class TypeScriptElementChangedListenerAdapater implements ITypeScriptElementChangedListener {

	@Override
	public void buildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath newBuildPath,
			ITypeScriptBuildPath oldBuildPath) {

	}

	@Override
	public void typeScriptVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion) {

	}

	@Override
	public void nodejsVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion) {

	}

}
