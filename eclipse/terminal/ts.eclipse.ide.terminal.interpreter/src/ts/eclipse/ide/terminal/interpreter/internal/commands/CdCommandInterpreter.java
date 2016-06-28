package ts.eclipse.ide.terminal.interpreter.internal.commands;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;

import ts.eclipse.ide.terminal.interpreter.AbstractCommandInterpreter;

public class CdCommandInterpreter extends AbstractCommandInterpreter {

	private final String path;

	public CdCommandInterpreter(String path, String workingDir) {
		super(workingDir);
		this.path = path;
	}

	@Override
	public void execute() {
		try {
			final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot()
					.findContainersForLocation(new Path(getWorkingDir() + "/" + path));
			if (c != null && c.length > 0) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
				final IViewPart view = page.findView(IPageLayout.ID_PROJECT_EXPLORER);

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						((ISetSelectionTarget) view).selectReveal(new StructuredSelection(c));
					}
				});
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTrace(String line) {
		// Do nothing
	}

}
