package ts.eclipse.ide.terminal.interpreter.internal.commands;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.AbstractCommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.internal.jobs.RefreshContainerJob;

public class DelCommandInterpreter extends AbstractCommandInterpreter {

	public DelCommandInterpreter(List<String> parameters, String workingDir) {
		super(parameters, workingDir);
	}

	@Override
	public void execute(List<String> parameters, String workingDir) {
		String path = parameters.get(0);
		final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot()
				.findContainersForLocation(new Path(workingDir + "/" + path));
		if (c != null && c.length > 0) {
			for (int i = 0; i < c.length; i++) {
				UIJob job = new RefreshContainerJob(c[i]);
				job.schedule();
			}
		}
	}

	@Override
	public void onTrace(String line) {
		// Do nothing
	}

}
