package ts.eclipse.ide.terminal.interpreter;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * Commons UI interpreter.
 *
 */
public class UIInterpreterHelper {

	/**
	 * Open in an editor the given file.
	 * 
	 * @param file
	 * @throws PartInitException
	 */
	public static void openFile(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		if (desc != null) {
			page.openEditor(new FileEditorInput(file), desc.getId());
		}
	}

	/**
	 * Select the given element in the Project Explorer.
	 * 
	 * @param element
	 */
	public static void selectRevealInProjectExplorer(Object element) {
		selectReveal(element, IPageLayout.ID_PROJECT_EXPLORER);
	}

	/**
	 * Select the given element in the view of the given id
	 * 
	 * @param element
	 */
	public static void selectReveal(Object element, String viewId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// Select in the Project Explorer the generated files.
		IViewPart view = page.findView(viewId);
		if (view instanceof ISetSelectionTarget) {
			((ISetSelectionTarget) view).selectReveal(new StructuredSelection(element));
		}
	}
}
