package ts.eclipse.ide.ui.utils;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.client.navbar.NavigationBarItem;
import ts.client.navbar.TextSpan;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;

public class EditorUtils {

	public static IEditorPart openInEditor(IFile file, boolean activate) {
		return openInEditor(file, null, null, null, null, activate);
	}

	public static IEditorPart openInEditor(IFile file, Integer startLine, Integer startOffset, Integer endLine,
			Integer endOffset, boolean activate) {
		IEditorPart editor = null;
		IWorkbenchPage page = TypeScriptUIPlugin.getActivePage();
		try {
			if (startLine != null && startLine > 0) {
				editor = IDE.openEditor(page, file, activate);
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor) {
					textEditor = (ITextEditor) editor;
				} else if (editor instanceof IAdaptable) {
					textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
				}
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

	public static IEditorPart openInEditor(File file, Integer startLine, Integer startOffset, Integer endLine,
			Integer endOffset, boolean activate) {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getPath()));
		IEditorPart editor = null;
		IWorkbenchPage page = TypeScriptUIPlugin.getActivePage();
		try {
			if (startLine != null && startLine > 0) {
				editor = IDE.openEditorOnFileStore(page, fileStore);
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor) {
					textEditor = (ITextEditor) editor;
				} else if (editor instanceof IAdaptable) {
					textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
				}
				if (textEditor != null) {
					IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
					int start = document.getLineOffset(startLine - 1) + startOffset - 1;
					int end = document.getLineOffset(endLine - 1) + endOffset - 1;
					int length = end - start;
					textEditor.selectAndReveal(start, length);
					page.activate(editor);
				}
			} else {
				editor = IDE.openEditorOnFileStore(page, fileStore);
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
		return getResource(editor.getEditorInput());
	}

	public static IResource getResource(IEditorInput input) {
		return (IResource) input.getAdapter(IResource.class);
	}

	/**
	 * Returns the resource of the given editor and null otherwise.
	 * 
	 * @param editor
	 * @return the resource of the given editor and null otherwise.
	 */
	public static IFileStore getFileStore(IEditorPart editor) {
		return (IFileStore) editor.getEditorInput().getAdapter(IFileStore.class);
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

	public static void openInEditor(IFile file, NavigationBarItem item) {
		if (!item.hasSpans()) {
			return;
		}

		TextSpan span = item.getSpans().get(0);
		openInEditor(file, span);
	}

	public static void openInEditor(IFile file, TextSpan span) {
		openInEditor(file, span.getStart().getLine(), span.getStart().getOffset(), span.getEnd().getLine(),
				span.getEnd().getOffset(), true);
	}

}
