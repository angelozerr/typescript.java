package ts.eclipse.ide.core.resources.buildpath;

import org.eclipse.core.runtime.IPath;

public interface ITypeScriptBuildPathEntry {

	public static final ITypeScriptBuildPathEntry[] EMPTY_ENTRY = new ITypeScriptBuildPathEntry[0];

	public static final IPath[] EMPTY_PATH = new IPath[0];

	IPath getPath();

}
