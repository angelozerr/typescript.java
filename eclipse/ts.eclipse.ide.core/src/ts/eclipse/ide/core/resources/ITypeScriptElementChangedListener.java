package ts.eclipse.ide.core.resources;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;

public interface ITypeScriptElementChangedListener {

	void buildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath newBuildPath,
			ITypeScriptBuildPath oldBuildPath);

	void typeScriptVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion);

	void nodejsVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion);
}
