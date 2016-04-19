package ts.eclipse.ide.core.resources.buildpath;

import java.util.List;

import org.eclipse.core.resources.IContainer;

public interface ITypeScriptBuildPath {

	List<IContainer> getContainers();

	List<ITypeScriptBuildPathEntry> getEntries();
}
