package ts.eclipse.ide.core.resources;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;

public interface ITypeScriptElementChangedListener {

	void buildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath newBuildPath,
			ITypeScriptBuildPath oldBuildPath);
}
