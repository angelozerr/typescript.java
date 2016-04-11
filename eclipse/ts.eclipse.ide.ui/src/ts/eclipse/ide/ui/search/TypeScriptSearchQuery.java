package ts.eclipse.ide.ui.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.internal.ui.text.SearchResultUpdater;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import ts.TypeScriptException;
import ts.client.references.ITypeScriptReferencesCollector;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;

public class TypeScriptSearchQuery implements ISearchQuery {

	private final IResource resource;
	private final int offset;

	private TypeScriptSearchResult searchResult;

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
			new SearchResultUpdater(result);
			searchResult = result;
		}
		return searchResult;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		try {
			IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(resource.getProject(), false);
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
					tsFile.references(offset, new ITypeScriptReferencesCollector() {
						@Override
						public void ref(String file, int startLine, int startOffset, int endLine, int endOffset,
								String lineText) throws TypeScriptException {
							System.err.println(file);
						}
					});
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

	public String getResultLabel(int nMatches) {
		return nMatches + " matches in " + resource.getProject().getFullPath().toString();
	}
}
