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

import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.navigator.ToggleLinkingAction;

import ts.client.navbar.NavigationBarItem;
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

	private static final String EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE = "TypeScriptEditor.SyncOutlineOnCursorMove"; //$NON-NLS-1$

	private CommonViewer fOutlineViewer;
	private ITypeScriptFile tsFile;

	private ListenerList fSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	private ListenerList fPostSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);

	private ToggleLinkingAction fToggleLinkingAction;

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
		if (fOutlineViewer != null)
			fOutlineViewer.setSelection(selection);
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

	}

	/**
	 * Register toolbar actions.
	 * 
	 * @param actionBars
	 */
	private void registerToolbarActions(IActionBars actionBars) {
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(new CollapseAllAction(this.fOutlineViewer));
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

}
