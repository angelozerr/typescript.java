package ts.eclipse.ide.jsdt.internal.ui.actions;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.wst.jsdt.ui.actions.SelectionDispatchAction;

import ts.TypeScriptNoContentAvailableException;
import ts.client.FileSpan;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.utils.EditorUtils;

public class OpenAction extends SelectionDispatchAction {

	private TypeScriptEditor fEditor;

	protected OpenAction(IWorkbenchSite site) {
		super(site);
	}

	public OpenAction(TypeScriptEditor editor) {
		this(editor.getEditorSite());
		fEditor = editor;
		setText(JSDTTypeScriptUIMessages.OpenDefinition_label);
		setEnabled(true);
		// setEnabled(EditorUtility.getEditorInputJavaElement(fEditor, false) !=
		// null);
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		// for (Iterator iter= selection.iterator(); iter.hasNext();) {
		// Object element= iter.next();
		// if (element instanceof ISourceReference)
		// continue;
		// if (element instanceof IFile)
		// continue;
		// if (JavaModelUtil.isOpenableStorage(element))
		// continue;
		// return false;
		// }
		return true;
	}

	@Override
	public void run(ITextSelection selection) {
		openDefinition(getResource(), selection.getOffset(), selection.getLength());
	}

	private void openDefinition(IResource resource, int offset, int length) {
		if (TypeScriptResourceUtil.canConsumeTsserver(resource)) {
			try {
				IProject project = resource.getProject();
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
				// IDocument document = textViewer.getDocument();
				IDocument document = null;
				IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);

				// Consume tsserver "definition" command and create hyperlink
				// file span are found.
				List<FileSpan> spans = tsFile.definition(offset).get(5000, TimeUnit.MILLISECONDS);
				if (spans.size() > 0) {
					FileSpan span = spans.get(0);
					EditorUtils.openInEditor(span);
				}
			} catch (ExecutionException e) {
				if (e.getCause() instanceof TypeScriptNoContentAvailableException) {
					// Ignore "No content available" error.

				}
				TypeScriptUIPlugin.log("Error while TypeScript Open definition", e);
			} catch (Exception e) {
				TypeScriptUIPlugin.log("Error while TypeScript Open definition", e);
			}
		}

	}

	TypeScriptEditor getEditor() {
		return fEditor;
	}

	IResource getResource() {
		return EditorUtils.getResource(getEditor());
	}
}
