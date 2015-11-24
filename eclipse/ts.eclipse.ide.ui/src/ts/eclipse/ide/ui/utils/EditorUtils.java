package ts.eclipse.ide.ui.utils;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.eclipse.ide.internal.ui.Trace;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;

public class EditorUtils {

	public static IEditorPart openInEditor(IFile file, Integer startLine, Integer startOffset, Integer endLine,
			Integer endOffset, boolean activate) {
		IEditorPart editor = null;
		IWorkbenchPage page = TypeScriptUIPlugin.getActivePage();
		try {
			if (startLine != null && startLine > 0) {
				editor = IDE.openEditor(page, file, activate);
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor)
					textEditor = (ITextEditor) editor;
				else if (editor instanceof IAdaptable)
					textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
				if (textEditor != null) {
					IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
					int start = document.getLineOffset(startLine - 1) + startOffset - 1;
					int end = document.getLineOffset(endLine - 1) + endOffset - 1;
					int length = end - start;
					textEditor.selectAndReveal(start, length);
					page.activate(editor);
				} else {
					IMarker marker = file.createMarker("org.eclipse.core.resources.textmarker");
					marker.setAttribute("lineNumber", startLine);
					editor = IDE.openEditor(page, marker, activate);
					marker.delete();
				}
			} else {
				editor = IDE.openEditor(page, file, activate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return editor;
	}

	/**
	 * Returns the file of the given editor and null otherwise.
	 * 
	 * @param editor
	 * @return the file of the given editor and null otherwise.
	 */
	public static IFile getFile(IEditorPart editor) {
		IResource resource = getResource(editor);
		return (resource != null && resource.getType() == IResource.FILE ? (IFile) resource : null);
	}

	/**
	 * Returns the resource of the given editor and null otherwise.
	 * 
	 * @param editor
	 * @return the resource of the given editor and null otherwise.
	 */
	public static IResource getResource(IEditorPart editor) {
		return (IResource) editor.getEditorInput().getAdapter(IResource.class);
	}

	public static ITextEditor getEditor(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(true);// ref.getPage().getActivePart();
		if (part != null && part instanceof ITextEditor) {
			return (ITextEditor) part;
		}
		return null;
	}

	public static IDocument getDocument(ITextEditor editor) {
		return (editor).getDocumentProvider().getDocument(editor.getEditorInput());
	}

	public static ISourceViewer getSourceViewer(IEditorPart editor) {
		if (editor == null) {
			return null;
		}

		ISourceViewer viewer = (ISourceViewer) editor.getAdapter(ITextOperationTarget.class);

		return viewer;
	}

	/**
	 * Returns the file from the given {@link IDocument}.
	 */
	public static IFile getFile(IDocument document) {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get
																						// the
																						// buffer
																						// manager
		ITextFileBuffer buffer = bufferManager.getTextFileBuffer(document);
		IPath location = buffer == null ? null : buffer.getLocation();
		if (location == null) {
			return null;
		}

		return ResourcesPlugin.getWorkspace().getRoot().getFile(location);
	}

	/**
	 * Returns the {@link IDocument} from the given file and null if it's not
	 * possible.
	 */
	public static IDocument getDocument(IFile file) {
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		IPath location = file.getLocation();
		boolean connected = false;
		try {
			ITextFileBuffer buffer = manager.getTextFileBuffer(location, LocationKind.NORMALIZE);
			if (buffer == null) {
				// no existing file buffer..create one
				manager.connect(location, LocationKind.NORMALIZE, new NullProgressMonitor());
				connected = true;
				buffer = manager.getTextFileBuffer(location, LocationKind.NORMALIZE);
				if (buffer == null) {
					return null;
				}
			}

			return buffer.getDocument();
		} catch (CoreException ce) {
			Trace.trace(Trace.SEVERE, "Error while get document from file", ce);
			return null;
		} finally {
			if (connected) {
				try {
					manager.disconnect(location, LocationKind.NORMALIZE, new NullProgressMonitor());
				} catch (CoreException e) {
					Trace.trace(Trace.SEVERE, "Error while get document from file", e);
				}
			}
		}
	}

}
