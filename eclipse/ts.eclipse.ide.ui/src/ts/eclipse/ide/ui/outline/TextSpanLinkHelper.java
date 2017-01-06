package ts.eclipse.ide.ui.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

import ts.client.navbar.NavigationBarItem;
import ts.client.navbar.NavigationTextSpan;
import ts.eclipse.ide.ui.utils.EditorUtils;

public class TextSpanLinkHelper implements ILinkHelper {

	@Override
	public void activateEditor(IWorkbenchPage page, IStructuredSelection selection) {
		NavigationTextSpan span = getSpan(selection);
		if (span != null) {
			IFile file = null;
			EditorUtils.openInEditor(file, span);
		}
	}

	private NavigationTextSpan getSpan(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Object element = selection.getFirstElement();
		if (element instanceof NavigationTextSpan) {
			return (NavigationTextSpan) element;
		}
		if (element instanceof NavigationBarItem) {
			NavigationBarItem item = (NavigationBarItem) element;
			if (item.hasSpans()) {
				return item.getSpans().get(0);
			}
		}
		return null;
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput input) {
		/*
		 * IJavaScriptElement element =
		 * JavaScriptUI.getEditorInputJavaElement(input); if (element == null) {
		 * IFile file = ResourceUtil.getFile(input); if (file != null) { element
		 * = JavaScriptCore.create(file); } } return (element != null) ? new
		 * StructuredSelection(element) : StructuredSelection.EMPTY;
		 */
		return StructuredSelection.EMPTY;
	}

}
