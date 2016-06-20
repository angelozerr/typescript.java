package ts.eclipse.ide.terminal.interpreter.npm.internal.commands;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;

public class NpmInstallCommandInterpreter implements ICommandInterpreter {

	@Override
	public void execute(List<String> parameters, String workingDir) {
		final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(new Path(workingDir));
		if (c != null && c.length > 0) {
			final IContainer container = c[0];
			new UIJob("Refresh npm project") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						container.refreshLocal(IResource.DEPTH_INFINITE, monitor);
						if (container.exists(new Path("node_modules"))) {
							IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
							final IViewPart view = page.findView(IPageLayout.ID_PROJECT_EXPLORER);
							((ISetSelectionTarget) view).selectReveal(
									new StructuredSelection(container.getFolder(new Path("node_modules"))));
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	@Override
	public void addLine(String line) {
		// Do nothing
	}

}
