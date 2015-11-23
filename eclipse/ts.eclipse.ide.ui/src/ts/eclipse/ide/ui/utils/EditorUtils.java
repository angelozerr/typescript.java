package ts.eclipse.ide.ui.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

public class EditorUtils {

	/**
	 * Returns the resource of the given editor and null otherwise.
	 * 
	 * @param editor
	 * @return the resource of the given editor and null otherwise.
	 */
	public static IResource getResource(IEditorPart editor) {
		return (IResource) editor.getEditorInput().getAdapter(IResource.class);
	}

	public static ISourceViewer getSourceViewer(IEditorPart editor) {
		if (editor == null) {
			return null;
		}

		ISourceViewer viewer = (ISourceViewer) editor.getAdapter(ITextOperationTarget.class);

		return viewer;
	}
}
