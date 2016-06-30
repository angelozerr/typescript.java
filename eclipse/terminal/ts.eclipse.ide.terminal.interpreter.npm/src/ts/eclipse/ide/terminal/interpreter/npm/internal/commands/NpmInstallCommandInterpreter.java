package ts.eclipse.ide.terminal.interpreter.npm.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.AbstractCommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.UIInterpreterHelper;
import ts.eclipse.ide.terminal.interpreter.npm.internal.NpmTerminalInterpreterPlugin;

public class NpmInstallCommandInterpreter extends AbstractCommandInterpreter {

	private final String NODE_MODULES = "node_modules";

	private final List<String> folders;

	public NpmInstallCommandInterpreter(String workingDir) {
		super(workingDir);
		this.folders = new ArrayList<String>();
	}

	@Override
	public void execute() {
		final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(getWorkingDirPath());
		if (c != null && c.length > 0) {
			final IContainer container = c[0];
			new UIJob("Refresh npm project") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						List<IResource> resources = getResources(monitor);
						if (resources.size() > 0) {
							UIInterpreterHelper.selectRevealInDefaultViews(resources.get(0));
						}
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, NpmTerminalInterpreterPlugin.PLUGIN_ID,
								"Error while collecting npm folders", e);
					}
					return Status.OK_STATUS;
				}

				private List<IResource> getResources(IProgressMonitor monitor) throws CoreException {
					List<IResource> resources = new ArrayList<IResource>();
					if (folders.size() > 0) {
						for (String folder : folders) {
							collectResource(folder, resources, monitor);
						}
					} else {
						collectResource(NODE_MODULES, resources, monitor);
					}

					return resources;
				}

				private void collectResource(String folder, List<IResource> resources, IProgressMonitor monitor)
						throws CoreException {
					IFolder resource = container.getFolder(new Path(folder));
					resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					if (resource.exists()) {
						resources.add(resource);
					}
				}
			}.schedule();

		}
	}

	@Override
	public void onTrace(String line) {
		int index = line.indexOf(NODE_MODULES);
		if (index != -1) {
			String folder = line.substring(index, line.length()).trim();
			folders.add(folder);
		}
	}

}
