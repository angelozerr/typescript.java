/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ts.TypeScriptException;
import ts.client.navbar.NavigationBarItem;
import ts.client.navbar.NavigationBarItemRoot;
import ts.client.navbar.NavigationTextSpan;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.INavbarListener;
import ts.resources.ITypeScriptFile;

/**
 * TypeScript Outline.
 *
 */
public class TypeScriptContentOutlinePage extends Page
		implements IContentOutlinePage, IPostSelectionProvider, INavbarListener {

	private static final String OUTLINE_COMMON_NAVIGATOR_ID = TypeScriptUIPlugin.PLUGIN_ID + ".outline"; //$NON-NLS-1$
	public static final String EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE = "TypeScriptEditor.SyncOutlineOnCursorMove"; //$NON-NLS-1$

	private CommonViewer fOutlineViewer;
	private ITypeScriptFile tsFile;

	private ListenerList fSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	private ListenerList fPostSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	private ITextSelection textSelection;

	private final IEditorOutlineFeatures editor;
	private boolean disposed;

	public TypeScriptContentOutlinePage(IEditorOutlineFeatures editor) {
		this.editor = editor;
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

		fOutlineViewer.setUseHashlookup(true);

		IActionBars actionBars = getSite().getActionBars();
		registerToolbarActions(actionBars);
	}

	public void setInput(ITypeScriptFile tsFile) {
		if (this.tsFile != null) {
			this.tsFile.removeNavbarListener(this);
		}
		if (tsFile != null) {
			this.tsFile = tsFile;
			this.tsFile.addNavbarListener(this);
		}
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
	public void navBarChanged(final NavigationBarItemRoot navbar) {
		if (fOutlineViewer != null && !fOutlineViewer.getTree().isDisposed()) {
			fOutlineViewer.getTree().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (fOutlineViewer.getTree().isDisposed()) {
						return;
					}
					boolean firstRefresh = fOutlineViewer.getInput() == null;
					List<TreePath> newExpandedTreePaths = !firstRefresh ? mapTreePaths(navbar) : null;
					// Refresh the tree
					fOutlineViewer.setInput(navbar);
					if (firstRefresh) {
						// first time, expand all the tree
						if (navbar.isNavTree()) {
							fOutlineViewer.expandToLevel(2);
						}
					} else {
						// second time, keep the last expansion of the tree
						fOutlineViewer.setExpandedTreePaths(newExpandedTreePaths.toArray(new TreePath[0]));
					}
					// update selection
					updateSelection();
				}

				private List<TreePath> mapTreePaths(NavigationBarItem navbar) {
					List<TreePath> treePaths = new ArrayList<TreePath>();
					for (TreePath treePath : fOutlineViewer.getExpandedTreePaths()) {
						TreePath newTreePath = TreePath.EMPTY;
						for (int i = 0; i < treePath.getSegmentCount(); i++) {
							NavigationBarItem segment = (NavigationBarItem) treePath.getSegment(i);
							NavigationBarItem newSegment = mapSegment(navbar, segment);
							if (newSegment != null) {
								newTreePath = newTreePath.createChildPath(newSegment);
							}
						}
						treePaths.add(newTreePath);
					}
					return treePaths;
				}

				private NavigationBarItem mapSegment(NavigationBarItem lexicalStructure, NavigationBarItem segment) {
					for (NavigationBarItem item : lexicalStructure.getChildItems()) {
						if (segment.getText().equals(item.getText())) {
							return item;
						}
					}
					return null;
				}

			});
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.addSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.removeSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.textSelection = selection instanceof ITextSelection ? (ITextSelection) selection : null;
		if (fOutlineViewer != null && textSelection == null) {
			fOutlineViewer.setSelection(selection);
		} else {
			updateSelection();
		}
	}

	private void updateSelection() {
		if (fOutlineViewer == null) {
			return;
		}
		if (textSelection == null) {
			return;
		}
		int offset = textSelection.getOffset();
		updateSelection(offset);
	}

	private void updateSelection(int offset) {
		if (fOutlineViewer.getInput() != null) {
			NavigationBarItemRoot root = (NavigationBarItemRoot) fOutlineViewer.getInput();

			try {
				NavigationBarItem bestItem = this.findBestMatch(root, offset, null, -1);
				if (bestItem != null) {
					fOutlineViewer.setSelection(new StructuredSelection(bestItem), true);
				}
			} catch (TypeScriptException e) {
				e.printStackTrace();
			}
			this.textSelection = null;
		}
	}

	private NavigationBarItem findBestMatch(NavigationBarItem navbar, int offset, NavigationBarItem bestItem,
			int bestSpanLength) throws TypeScriptException {
		if (!navbar.hasChildItems()) {
			return bestItem;
		}
		for (NavigationBarItem navigateToItem : navbar.getChildItems()) {
			List<NavigationTextSpan> spans = navigateToItem.getSpans();

			for (NavigationTextSpan span : spans) {
				if (span.contains(offset)) {
					// the best item is the one with the smallest span which
					// contains the offset
					if (bestItem == null || (span.getLength() <= bestSpanLength)) {
						bestItem = navigateToItem;
						bestSpanLength = span.getLength();
					}
				}
			}

			bestItem = this.findBestMatch(navigateToItem, offset, bestItem, bestSpanLength);
		}

		return bestItem;
	}

	@Override
	public ISelection getSelection() {
		if (fOutlineViewer == null)
			return StructuredSelection.EMPTY;
		return fOutlineViewer.getSelection();
	}

	@Override
	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		if (fOutlineViewer != null)
			fOutlineViewer.addPostSelectionChangedListener(listener);
		else
			fPostSelectionChangedListeners.add(listener);
	}

	@Override
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

		disposed = true;
	}

	/**
	 * Register toolbar actions.
	 * 
	 * @param actionBars
	 */
	private void registerToolbarActions(IActionBars actionBars) {
		// Toolbar
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(new CollapseAllAction(this.fOutlineViewer));

		// Menu
		IMenuManager viewMenuManager = actionBars.getMenuManager();
		viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
		viewMenuManager.add(new ToggleLinkingAction());

	}

	/**
	 * Collapse all action
	 *
	 */
	private class CollapseAllAction extends Action {

		private final TreeViewer viewer;

		CollapseAllAction(TreeViewer viewer) {
			super(TypeScriptUIMessages.TypeScriptContentOutlinePage_CollapseAllAction_label);
			setDescription(TypeScriptUIMessages.TypeScriptContentOutlinePage_CollapseAllAction_description);
			setToolTipText(TypeScriptUIMessages.TypeScriptContentOutlinePage_CollapseAllAction_tooltip);
			super.setImageDescriptor(
					TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_COLLAPSE_ALL_ENABLED));
			super.setDisabledImageDescriptor(
					TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_COLLAPSE_ALL_DISABLED));
			this.viewer = viewer;
		}

		public void run() {
			this.viewer.collapseAll();
		}
	}

	/**
	 * Link with editor.
	 *
	 */
	private class ToggleLinkingAction extends Action {
		public ToggleLinkingAction() {
			super(TypeScriptUIMessages.TypeScriptContentOutlinePage_ToggleLinkingAction_label);
			setDescription(TypeScriptUIMessages.TypeScriptContentOutlinePage_ToggleLinkingAction_description);
			setToolTipText(TypeScriptUIMessages.TypeScriptContentOutlinePage_ToggleLinkingAction_tooltip);
			super.setImageDescriptor(
					TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_SYNCED_ENABLED));
			super.setDisabledImageDescriptor(
					TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_SYNCED_DISABLED));
			setChecked(isLinkingEnabled());
		}

		@Override
		public void run() {
			TypeScriptUIPlugin.getDefault().getPreferenceStore().setValue(EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE,
					isChecked());
			if (isChecked() && editor != null) {
				int offset = editor.getCursorOffset();
				updateSelection(offset);
			}
		}
	}

	public boolean isLinkingEnabled() {
		return TypeScriptUIPlugin.getDefault().getPreferenceStore().getBoolean(EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE);
	}

	public boolean isDisposed() {
		return disposed;
	}
}
