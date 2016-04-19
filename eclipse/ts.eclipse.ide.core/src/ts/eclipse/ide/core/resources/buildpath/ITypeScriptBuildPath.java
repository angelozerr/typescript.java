package ts.eclipse.ide.core.resources.buildpath;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

public interface ITypeScriptBuildPath {

	List<IContainer> getContainers();

	List<ITypeScriptBuildPathEntry> getEntries();

	boolean isInScope(IResource resource);

	IContainer getContainer(IResource resource);
}
