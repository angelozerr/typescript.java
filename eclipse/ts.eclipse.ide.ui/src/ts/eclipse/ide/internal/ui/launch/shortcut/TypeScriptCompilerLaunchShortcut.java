package ts.eclipse.ide.internal.ui.launch.shortcut;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import ts.eclipse.ide.ui.launch.TypeScriptCompilerLaunchHelper;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class TypeScriptCompilerLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			IContainer container = getContainer(element);
			if (container != null) {
				TypeScriptCompilerLaunchHelper.launch(container, mode);
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {

	}
	
	private IContainer getContainer(Object element) {
		if (element instanceof IContainer) {
			return (IContainer) element;
		} else if (element instanceof ITypeScriptRootContainer) {
			return ((ITypeScriptRootContainer) element).getContainer();
		} else if (element instanceof IFile && TypeScriptResourceUtil.isTsConfigFile((IFile) element)) {
			return ((IFile) element).getParent();
		}
		return null;
	}

}
