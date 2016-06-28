package ts.eclipse.ide.terminal.interpreter.npm.internal.commands;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.AbstractCommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.UIInterpreterHelper;

public class NpmInstallCommandInterpreter extends AbstractCommandInterpreter {

	private final IPath NODE_MODULES_PATH = new Path("node_modules");

	public NpmInstallCommandInterpreter(String workingDir) {
		super(workingDir);
	}

	@Override
	public void execute() {
		final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(getWorkingDirPath());
		if (c != null && c.length > 0) {
			final IContainer container = c[0];
			new UIJob("Refresh npm project") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (container.exists(NODE_MODULES_PATH)) {
						UIInterpreterHelper.selectRevealInProjectExplorer(container.getFolder(NODE_MODULES_PATH));
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

}
