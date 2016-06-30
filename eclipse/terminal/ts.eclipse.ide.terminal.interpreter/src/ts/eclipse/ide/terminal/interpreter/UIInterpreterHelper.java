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

	private final static String[] DEFAULT_VIEW_IDS = { IPageLayout.ID_PROJECT_EXPLORER,
			"org.eclipse.ui.views.ResourceNavigator" };

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
	 * Select the given element in the default view (Project Explorer,
	 * Navigator).
	 * 
	 * @param element
	 */
	public static void selectRevealInDefaultViews(Object element) {
		for (String viewId : DEFAULT_VIEW_IDS) {
			selectReveal(element, viewId);
		}
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
