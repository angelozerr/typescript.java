package ts.eclipse.ide.internal.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class TypeScriptCompileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			for (Object receiver : ((IStructuredSelection) selection).toList()) {
				IContainer container = getContainer(receiver);
				if (container != null) {
					try {
						IIDETypeScriptProject tsProject = TypeScriptResourceUtil
								.getTypeScriptProject(container.getProject());
						if (tsProject.getTypeScriptBuildPath().isRootContainer(container)) {
							tsProject.getCompiler().compile(container);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private IContainer getContainer(Object receiver) {
		if (receiver instanceof IContainer) {
			return (IContainer) receiver;
		} else if (receiver instanceof ITypeScriptRootContainer) {
			return ((ITypeScriptRootContainer) receiver).getContainer();
		}
		return null;
	}

}
