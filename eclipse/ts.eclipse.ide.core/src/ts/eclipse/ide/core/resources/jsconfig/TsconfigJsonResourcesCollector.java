package ts.eclipse.ide.core.resources.jsconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;

public class TsconfigJsonResourcesCollector extends AbstractTsconfigJsonCollector {

	private final boolean collectFile;
	private final List<IResource> resources;

	public TsconfigJsonResourcesCollector(boolean collectFile) {
		this.collectFile = collectFile;
		this.resources = new ArrayList<IResource>();
	}

	@Override
	protected void collect(IResource file) {
		if (collectFile) {
			resources.add(file);
		} else {
			resources.add(file.getParent());
		}
	}

	public List<IResource> getResources() {
		return resources;
	}

}
