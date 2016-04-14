package ts.eclipse.ide.internal.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ts.eclipse.ide.core.builder.TypeScriptBuilder;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class AddTypeScriptBuilderHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			for (Object obj : ((IStructuredSelection) selection).toList()) {
				if (obj instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
					if (project != null) {
						try {
							if (TypeScriptResourceUtil.hasTypeScriptBuilder(project)) {
								return null;
							}
							IProjectDescription description = project.getDescription();
							ICommand[] commands = description.getBuildSpec();
							ICommand[] newCommands = new ICommand[commands.length + 1];
							System.arraycopy(commands, 0, newCommands, 0, commands.length);
							ICommand command = description.newCommand();
							command.setBuilderName(TypeScriptBuilder.ID);
							newCommands[newCommands.length - 1] = command;
							description.setBuildSpec(newCommands);
							project.setDescription(description, null);

						} catch (CoreException e) {
							throw new ExecutionException(e.getMessage(), e);
						}
					}
				}

			}
		}
		return null;
	}

}
