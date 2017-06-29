package ts.eclipse.ide.ui.utils;

import java.io.File;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.client.FileSpan;
import ts.client.TextSpan;
import ts.client.codefixes.FileCodeEdits;
import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.core.utils.DocumentUtils;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.utils.StringUtils;

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
					int start = DocumentUtils.getPosition(document, startLine, startOffset);
					int end = DocumentUtils.getPosition(document, endLine, endOffset);
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

	public static void openInEditor(File file, TextSpan span) {
		openInEditor(file, span.getStart().getLine(), span.getStart().getOffset(), span.getEnd().getLine(),
				span.getEnd().getOffset(), true);

	}

	public static void openInEditor(FileSpan span) {
		IFile file = WorkbenchResourceUtil.findFileFromWorkspace(span.getFile());
		if (file != null) {
			EditorUtils.openInEditor(file, span);
		} else {
			File fsFile = WorkbenchResourceUtil.findFileFormFileSystem(span.getFile());
			if (fsFile != null) {
				EditorUtils.openInEditor(fsFile, span);
			}
		}
	}

	public static Position getPosition(IFile file, TextSpan textSpan) throws BadLocationException {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer buffer = bufferManager.getTextFileBuffer(file.getLocation(), LocationKind.IFILE);
		if (buffer != null) {
			return getPosition(buffer.getDocument(), textSpan);
		}
		IDocumentProvider provider = new TextFileDocumentProvider();
		try {
			provider.connect(file);
			IDocument document = provider.getDocument(file);
			if (document != null) {
				return getPosition(document, textSpan);
			}
		} catch (CoreException e) {
		} finally {
			provider.disconnect(file);
		}
		return null;
	}

	public static Position getPosition(IDocument document, TextSpan textSpan) throws BadLocationException {
		int startLine = textSpan.getStart().getLine();
		int startOffset = textSpan.getStart().getOffset();
		int start = document.getLineOffset(startLine - 1) + startOffset - 1;
		int endLine = textSpan.getEnd().getLine();
		int endOffset = textSpan.getEnd().getOffset();
		int end = document.getLineOffset(endLine - 1) + endOffset - 1;
		int length = end - start;
		return new Position(start, length);
	}

	public static void applyEdit(List<FileCodeEdits> filesCodeEdits, IDocument document, String originalFileName) {
		for (FileCodeEdits codeEdits : filesCodeEdits) {
			EditorUtils.applyEdit(codeEdits, document, originalFileName);
		}
	}

	public static void applyEdit(FileCodeEdits codeEdits, IDocument document, String originalFileName) {
		IDocument documentToUpdate = getDocumentToUpdate(codeEdits.getFileName(), document, originalFileName);
		if (documentToUpdate != null) {
			try {
				DocumentUtils.applyEdits(documentToUpdate, codeEdits.getTextChanges());
			} catch (Exception e) {
				TypeScriptUIPlugin.log(e);
			}
		}
	}

	private static IDocument getDocumentToUpdate(String fileName, IDocument document, String originalFileName) {
		if (StringUtils.isEmpty(fileName) || originalFileName.equals(fileName)) {
			// Update the document of the fileName which have opened the
			// QuickFix
			return document;
		}
		IFile file = WorkbenchResourceUtil.findFileFromWorkspace(fileName);
		if (file != null && file.exists()) {
			EditorUtils.openInEditor(file, true);
			return TypeScriptResourceUtil.getDocument(file);
		}
		return null;
	}
}
