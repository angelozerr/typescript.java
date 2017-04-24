package ts.eclipse.ide.jsdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.wst.jsdt.ui.actions.SelectionDispatchAction;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.refactoring.RefactoringMessages;
import ts.eclipse.ide.jsdt.ui.IContextMenuConstants;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.jsdt.ui.actions.TypeScriptActionConstants;

/**
 * Action group that adds refactor actions (for example 'Rename', 'Move') to a
 * context menu and the global menu bar.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactorActionGroup extends ActionGroup {

	/**
	 * Pop-up menu: id of the refactor sub menu (value
	 * <code>ts.eclipse.ide.jsdt.ui.refactoring.menu</code>).
	 */
	public static final String MENU_ID = "ts.eclipse.ide.jsdt.ui.refactoring.menu"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the reorg group of the refactor sub menu (value
	 * <code>reorgGroup</code>).
	 *
	 */
	public static final String GROUP_REORG = "reorgGroup"; //$NON-NLS-1$

	private TypeScriptEditor fEditor;
	private String fGroupName = IContextMenuConstants.GROUP_REORGANIZE;

	private final List<SelectionDispatchAction> fActions = new ArrayList<SelectionDispatchAction>();

	private static class NoActionAvailable extends Action {
		public NoActionAvailable() {
			setEnabled(true);
			setText(RefactoringMessages.RefactorActionGroup_no_refactoring_available);
		}
	}

	private Action fNoActionAvailable = new NoActionAvailable();

	private final ISelectionProvider fSelectionProvider;

	private RenameAction fRenameAction;

	private IEditorSite fSite;

	public RefactorActionGroup(TypeScriptEditor editor, String groupName) {
		fEditor = editor;
		fGroupName = groupName;
		fSite = editor.getEditorSite();
		fSelectionProvider = fSite.getSelectionProvider();

		ISelectionProvider provider = editor.getSelectionProvider();
		ISelection selection = provider.getSelection();

		fRenameAction = new RenameAction(editor);
		initAction(fRenameAction, selection, ITypeScriptEditorActionDefinitionIds.RENAME_ELEMENT);
		editor.setAction("RenameElement", fRenameAction); //$NON-NLS-1$

	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(TypeScriptActionConstants.RENAME, fRenameAction);
	}

	private void initAction(SelectionDispatchAction action, ISelection selection, String actionDefinitionId) {
		initUpdatingAction(action, null, null, selection, actionDefinitionId);
	}

	/**
	 * Sets actionDefinitionId, updates enablement, adds to fActions, and adds
	 * selection changed listener if provider is not <code>null</code>.
	 *
	 * @param action
	 *            the action
	 * @param provider
	 *            can be <code>null</code>
	 * @param specialProvider
	 *            a special selection provider or <code>null</code>
	 * @param selection
	 *            the selection
	 * @param actionDefinitionId
	 *            the action definition id
	 */
	private void initUpdatingAction(SelectionDispatchAction action, ISelectionProvider provider,
			ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId) {
		action.setActionDefinitionId(actionDefinitionId);
		action.update(selection);
		if (provider != null)
			provider.addSelectionChangedListener(action);
		if (specialProvider != null)
			action.setSpecialSelectionProvider(specialProvider);
		fActions.add(action);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		addRefactorSubmenu(menu);
	}

	private void addRefactorSubmenu(IMenuManager menu) {
		MenuManager refactorSubmenu = new MenuManager(RefactoringMessages.RefactorMenu_label, MENU_ID);
		// refactorSubmenu.setActionDefinitionId(QUICK_MENU_ID);
		if (fEditor != null) {
			// final ITypeRoot element= getEditorInput();
			// if (element != null && ActionUtil.isOnBuildPath(element)) {
			refactorSubmenu.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					refactorMenuShown(manager);
				}
			});
			refactorSubmenu.add(fNoActionAvailable);
			menu.appendToGroup(fGroupName, refactorSubmenu);
			// }
		} else {
			ISelection selection = fSelectionProvider.getSelection();
			for (Iterator<SelectionDispatchAction> iter = fActions.iterator(); iter.hasNext();) {
				iter.next().update(selection);
			}
			if (fillRefactorMenu(refactorSubmenu) > 0)
				menu.appendToGroup(fGroupName, refactorSubmenu);
		}
	}

	private int fillRefactorMenu(IMenuManager refactorSubmenu) {
		int added = 0;
		refactorSubmenu.add(new Separator(GROUP_REORG));
		added += addAction(refactorSubmenu, fRenameAction);
		return added;
	}

	private int addAction(IMenuManager menu, IAction action) {
		if (action != null && action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 0;
	}

	private void refactorMenuShown(IMenuManager refactorSubmenu) {
		// we know that we have an MenuManager since we created it in
		// addRefactorSubmenu.
		Menu menu = ((MenuManager) refactorSubmenu).getMenu();
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuHidden(MenuEvent e) {
				refactorMenuHidden();
			}
		});
		ITextSelection textSelection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		// JavaTextSelection javaSelection= new
		// JavaTextSelection(getEditorInput(), getDocument(),
		// textSelection.getOffset(), textSelection.getLength());

		for (Iterator<SelectionDispatchAction> iter = fActions.iterator(); iter.hasNext();) {
			SelectionDispatchAction action = iter.next();
			action.update(textSelection);
		}
		refactorSubmenu.removeAll();
		if (fillRefactorMenu(refactorSubmenu) == 0)
			refactorSubmenu.add(fNoActionAvailable);
	}

	private void refactorMenuHidden() {
		ITextSelection textSelection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		for (Iterator<SelectionDispatchAction> iter = fActions.iterator(); iter.hasNext();) {
			SelectionDispatchAction action = iter.next();
			action.update(textSelection);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeAction(fRenameAction, fSelectionProvider);
	}

	private void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) {
		if (action != null)
			provider.removeSelectionChangedListener(action);
	}

}
