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

@Deprecated
public class RemoveTypeScriptBuilderHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			for (Object obj : ((IStructuredSelection) selection).toList()) {
				if (obj instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
					if (project != null) {
						try {
							IProjectDescription description = project.getDescription();
							ICommand[] commands = description.getBuildSpec();
							for (int i = 0; i < commands.length; i++) {
								if (TypeScriptBuilder.ID.equals(commands[i].getBuilderName())) {
									// Remove the builder
									ICommand[] newCommands = new ICommand[commands.length - 1];
									System.arraycopy(commands, 0, newCommands, 0, i);
									System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
									description.setBuildSpec(newCommands);
									project.setDescription(description, null);
								}
							}
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
