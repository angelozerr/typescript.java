package ts.eclipse.ide.jsdt.internal.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.wst.jsdt.internal.ui.search.SearchMessages;
import org.eclipse.wst.jsdt.internal.ui.search.SearchUtil;

import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.ui.search.TypeScriptSearchQuery;

public class FindReferencesInProjectAction extends FindAction {

	FindReferencesInProjectAction(TypeScriptEditor editor) {
		super(editor);
	}

	public FindReferencesInProjectAction(IWorkbenchSite site) {
		super(site);
	}

	@Override
	void init() {
		setText(SearchMessages.Search_FindReferencesInProjectAction_label);
		setToolTipText(SearchMessages.Search_FindReferencesInProjectAction_tooltip);
	}

	@Override
	public void run(ITextSelection selection) {
		findReferences(getResource(), selection.getOffset(), selection.getLength());
	}

	private void findReferences(IResource resource, int offset, int length) {
		TypeScriptSearchQuery query = new TypeScriptSearchQuery(resource, offset);
		if (query.canRunInBackground()) {
			/*
			 * This indirection with Object as parameter is needed to prevent
			 * the loading of the Search plug-in: the VM verifies the method
			 * call and hence loads the types used in the method signature,
			 * eventually triggering the loading of a plug-in (in this case
			 * ISearchQuery results in Search plug-in being loaded).
			 */
			SearchUtil.runQueryInBackground(query);
		} else {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			/*
			 * This indirection with Object as parameter is needed to prevent
			 * the loading of the Search plug-in: the VM verifies the method
			 * call and hence loads the types used in the method signature,
			 * eventually triggering the loading of a plug-in (in this case it
			 * would be ISearchQuery).
			 */
			IStatus status = SearchUtil.runQueryInForeground(progressService, query);
			if (status.matches(IStatus.ERROR | IStatus.INFO | IStatus.WARNING)) {
				ErrorDialog.openError(getShell(), SearchMessages.Search_Error_search_title,
						SearchMessages.Search_Error_search_message, status);
			}
		}
	}
}
