package ts.eclipse.ide.ui.search;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import ts.client.references.ReferencesResponseBody;
import ts.client.references.ReferencesResponseItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.search.LineElement;
import ts.eclipse.ide.internal.ui.search.TypeScriptMatch;

public class TypeScriptSearchQuery implements ISearchQuery {

	private final IResource resource;
	private final int offset;

	private TypeScriptSearchResult searchResult;
	private long startTime = 0;
	private long endTime = 0;

	public TypeScriptSearchQuery(IResource resource, int offset) {
		this.resource = resource;
		this.offset = offset;
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public String getLabel() {
		return TypeScriptUIMessages.TypeScriptSearchQuery_label;
	}

	@Override
	public ISearchResult getSearchResult() {
		if (searchResult == null) {
			TypeScriptSearchResult result = new TypeScriptSearchResult(this);
			// new SearchResultUpdater(result);
			searchResult = result;
		}
		return searchResult;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		startTime = System.currentTimeMillis();
		final TypeScriptSearchResult tsResult = (TypeScriptSearchResult) getSearchResult();
		tsResult.removeAll();

		try {
			IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(resource.getProject(), false);
			IIDETypeScriptFile tsFile = null;
			boolean wasOpened = false;
			try {
				// open ts file if needed
				tsFile = tsProject.getOpenedFile(resource);
				if (tsFile != null) {
					wasOpened = true;
				} else {
					tsFile = tsProject.openFile(resource, null);
				}
				if (tsFile != null) {
					// Find references
					ReferencesResponseBody references = tsFile.references(offset).get(20000, TimeUnit.MILLISECONDS);
					for (ReferencesResponseItem reference : references.getRefs()) {
						addRef(reference, tsResult);
					}
				}
			} finally {
				// close ts file if needed
				if (!wasOpened && tsFile != null) {
					tsFile.close();
				}
			}

		} catch (Exception e) {
			throw new OperationCanceledException(e.getMessage());
			// IStatus status = new Status(IStatus.ERROR,
			// JSDTTypeScriptUIPlugin.PLUGIN_ID,
			// TypeScriptUIMessages.FindReferencesInProjectAction_error, e);
			// ErrorDialog.openError(getShell(),
			// TypeScriptUIMessages.FindReferencesInProjectAction_error_title,
			// e.getMessage(), status);
			// e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

	private void addRef(ReferencesResponseItem reference, TypeScriptSearchResult tsResult) {
		String filename = reference.getFile();
		IFile tsFile = WorkbenchResourceUtil.findFileFromWorkspace(filename);
		if (tsFile != null) {
			int startLine = reference.getStart().getLine();
			int startLineOffset = reference.getStart().getOffset();
			int endLine = reference.getEnd().getLine();
			int endLineOffset = reference.getEnd().getOffset();
			String lineText = reference.getLineText();
			try {
				IDocument document = TypeScriptResourceUtil.getDocument(tsFile);
				int lineNumber = startLine - 1;
				int lineStartOffset = startLineOffset - 1;
				int beginOfLineStartOffset = document.getLineOffset(lineNumber);
				int startOffset = beginOfLineStartOffset + lineStartOffset;
				int endOffset = document.getLineOffset(endLine - 1) + (endLineOffset - 1);
				int length = endOffset - startOffset;

				LineElement lineEntry = new LineElement(tsFile, lineNumber, beginOfLineStartOffset, lineText);
				tsResult.addMatch(new TypeScriptMatch(tsFile, startOffset, length, lineEntry));

			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public String getResultLabel(int nMatches) {
		long time = 0;
		if (startTime > 0) {
			if (endTime > 0) {
				time = endTime - startTime;
			} else {
				time = System.currentTimeMillis() - startTime;
			}
		}
		Object[] values = { nMatches, time };
		return NLS.bind(TypeScriptUIMessages.TypeScriptSearchQuery_result, values);
	}

}
