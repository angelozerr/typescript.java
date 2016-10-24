package ts.eclipse.ide.internal.ui.launch.shortcut;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.launch.TypeScriptCompilerLaunchHelper;

public class TypeScriptCompilerLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			IFile tsconfigFile = getTsconfigFile(element);
			if (tsconfigFile != null) {
				TypeScriptCompilerLaunchHelper.launch(tsconfigFile, mode);
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {

	}

	private IFile getTsconfigFile(Object element) {
		if (element instanceof ITsconfigBuildPath) {
			return ((ITsconfigBuildPath) element).getTsconfigFile();
		} else if (element instanceof IFile && TypeScriptResourceUtil.isTsConfigFile((IFile) element)) {
			return ((IFile) element);
		}
		return null;
	}

}
