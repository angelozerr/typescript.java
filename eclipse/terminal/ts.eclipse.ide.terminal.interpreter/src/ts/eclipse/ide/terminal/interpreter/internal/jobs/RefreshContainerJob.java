package ts.eclipse.ide.terminal.interpreter.internal.jobs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.UIInterpreterHelper;
import ts.eclipse.ide.terminal.interpreter.internal.TerminalInterpreterPlugin;

public class RefreshContainerJob extends UIJob {

	private final IContainer container;
	private final boolean refresh;

	public RefreshContainerJob(IContainer container, boolean refresh) {
		super("Refresh container job");
		this.container = container;
		this.refresh = refresh;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (refresh) {
			try {
				container.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, TerminalInterpreterPlugin.PLUGIN_ID,
						"Error while refreshing container", e);
			}
		}
		// Select the container in the Project Explorer
		UIInterpreterHelper.selectRevealInDefaultViews(container);
		return Status.OK_STATUS;
	}
}