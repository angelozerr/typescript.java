package ts.eclipse.ide.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

public class TypeScriptSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {

	private static final Match[] NO_MATCHES = new Match[0];

	/**
	 * Query that fills result matches
	 */
	private final TypeScriptSearchQuery query;

	/**
	 * Constructor
	 * 
	 * @param query
	 *            corresponding query
	 */
	public TypeScriptSearchResult(TypeScriptSearchQuery query) {
		super();
		this.query = query;
	}

	@Override
	public String getLabel() {
		return query.getResultLabel(getMatchCount());
	}

	@Override
	public String getTooltip() {
		return getLabel();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;//TypeScriptUIImages.getImageDescriptor(TypeScriptImageKeys.IMG_SEARCH_REF_OBJ);
	}

	@Override
	public ISearchQuery getQuery() {
		return query;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return getMatches(file);
	}

	@Override
	public IFile getFile(Object element) {
		if (element instanceof IFile) {
			return (IFile) element;
		}
		return null;
	}

	@Override
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei = editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi = (IFileEditorInput) ei;
			return match.getElement().equals(fi.getFile());
		}
		return false;
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		IEditorInput ei = editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi = (IFileEditorInput) ei;
			return getMatches(fi.getFile());
		}
		return NO_MATCHES;
	}
}
