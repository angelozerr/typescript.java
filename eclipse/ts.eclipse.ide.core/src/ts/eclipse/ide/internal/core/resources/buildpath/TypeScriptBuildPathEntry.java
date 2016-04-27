package ts.eclipse.ide.internal.core.resources.buildpath;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPathEntry;

public class TypeScriptBuildPathEntry implements ITypeScriptBuildPathEntry {

	private final IPath path;

	public TypeScriptBuildPathEntry(String path) {
		this(new Path(path));
	}

	public TypeScriptBuildPathEntry(IPath path) {
		this.path = path;
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITypeScriptBuildPathEntry) {
			ITypeScriptBuildPathEntry entry = (ITypeScriptBuildPathEntry) obj;
			return entry.getPath().equals(getPath());
		}
		return false;
	}
}
