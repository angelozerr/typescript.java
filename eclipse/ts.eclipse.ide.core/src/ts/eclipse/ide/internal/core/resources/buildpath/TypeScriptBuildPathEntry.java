package ts.eclipse.ide.internal.core.resources.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPathEntry;

public class TypeScriptBuildPathEntry implements ITypeScriptBuildPathEntry {

	private final IPath path;
	private final List<IPath> inclusionPatterns;
	private final List<IPath> exclusionPatterns;

	public TypeScriptBuildPathEntry(String path, String inclusionPatterns, String exclusionPatterns) {
		this(new Path(path));
	}

	public TypeScriptBuildPathEntry(IPath path) {
		this.path = path;
		this.inclusionPatterns = new ArrayList<IPath>();
		this.exclusionPatterns = new ArrayList<IPath>();
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public IPath[] getInclusionPatterns() {
		return inclusionPatterns.toArray(EMPTY_PATH);
	}

	public void addInclusionPattern() {

	}

	@Override
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns.toArray(EMPTY_PATH);
	}

}
