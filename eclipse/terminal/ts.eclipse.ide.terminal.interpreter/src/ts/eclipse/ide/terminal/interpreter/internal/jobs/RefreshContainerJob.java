package ts.eclipse.ide.terminal.interpreter.internal.jobs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.internal.TerminalInterpreterPlugin;

public class RefreshContainerJob extends UIJob {

	private final IContainer container;

	public RefreshContainerJob(IContainer container) {
		super("Refresh container job");
		this.container = container;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		try {
			container.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, TerminalInterpreterPlugin.PLUGIN_ID, "Error while refreshing container",
					e);
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IViewPart view = page.findView(IPageLayout.ID_PROJECT_EXPLORER);
		((ISetSelectionTarget) view).selectReveal(new StructuredSelection(container));
		return Status.OK_STATUS;
	}
}