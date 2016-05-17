package ts.eclipse.ide.ui.outline;

import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.INavbarListener;
import ts.resources.ITypeScriptFile;

public class TypeScriptContentOutlinePage extends Page implements IContentOutlinePage, INavbarListener {

	private static final String OUTLINE_COMMON_NAVIGATOR_ID = TypeScriptUIPlugin.PLUGIN_ID + ".outline"; //$NON-NLS-1$

	private CommonViewer fOutlineViewer;
	private ITypeScriptFile tsFile;

	private ListenerList fSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	private ListenerList fPostSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);

	public TypeScriptContentOutlinePage() {
	}

	@Override
	public void createControl(Composite parent) {
		fOutlineViewer = new CommonViewer(OUTLINE_COMMON_NAVIGATOR_ID, parent, SWT.MULTI);
		// seems like common filters need to be explicitly added
		for (ViewerFilter filter : fOutlineViewer.getNavigatorContentService().getFilterService()
				.getVisibleFilters(true)) {
			this.fOutlineViewer.addFilter(filter);
		}

		Object[] listeners = fSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			fSelectionChangedListeners.remove(listeners[i]);
			fOutlineViewer.addSelectionChangedListener((ISelectionChangedListener) listeners[i]);
		}

		listeners = fPostSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			fPostSelectionChangedListeners.remove(listeners[i]);
			fOutlineViewer.addPostSelectionChangedListener((ISelectionChangedListener) listeners[i]);
		}

		fOutlineViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

	}

	public void setInput(ITypeScriptFile tsFile) {
		if (this.tsFile != null) {
			this.tsFile.removeNavbarListener(this);
		}

		this.tsFile = tsFile;
		this.tsFile.addNavbarListener(this);
	}

	public Control getControl() {
		if (fOutlineViewer != null)
			return fOutlineViewer.getControl();
		return null;
	}

	@Override
	public void setFocus() {
		if (fOutlineViewer != null) {
			fOutlineViewer.getControl().setFocus();
		}
	}

	@Override
	public void navBarChanged(final List<NavigationBarItem> items) {
		if (fOutlineViewer != null && !fOutlineViewer.getTree().isDisposed()) {
			fOutlineViewer.getTree().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					fOutlineViewer.setInput(items);
				}
			});
		}
	}

	/*
	 * @see
	 * ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.addSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.add(listener);
	}

	/*
	 * @see ISelectionProvider#removeSelectionChangedListener(
	 * ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.removeSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.remove(listener);
	}

	/*
	 * @see ISelectionProvider#setSelection(ISelection)
	 */
	public void setSelection(ISelection selection) {
		if (fOutlineViewer != null)
			fOutlineViewer.setSelection(selection);
	}

	/*
	 * @see ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		if (fOutlineViewer == null)
			return StructuredSelection.EMPTY;
		return fOutlineViewer.getSelection();
	}

	/*
	 * @see org.eclipse.jface.text.IPostSelectionProvider#
	 * addPostSelectionChangedListener(org.eclipse.jface.viewers.
	 * ISelectionChangedListener)
	 */
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.addPostSelectionChangedListener(listener);
		else
			fPostSelectionChangedListeners.add(listener);
	}

	/*
	 * @see org.eclipse.jface.text.IPostSelectionProvider#
	 * removePostSelectionChangedListener(org.eclipse.jface.viewers.
	 * ISelectionChangedListener)
	 */
	public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.removePostSelectionChangedListener(listener);
		else
			fPostSelectionChangedListeners.remove(listener);
	}

	@Override
	public void dispose() {
		super.dispose();

		fSelectionChangedListeners.clear();
		fSelectionChangedListeners = null;

		fPostSelectionChangedListeners.clear();
		fPostSelectionChangedListeners = null;

	}

}
