package ts.eclipse.ide.jsdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.jsdt.ui.actions.SelectionDispatchAction;

import ts.client.CommandNames;
import ts.client.refactors.ApplicableRefactorInfo;
import ts.client.refactors.RefactorActionInfo;
import ts.client.refactors.RefactorEditInfo;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.refactoring.RefactoringMessages;
import ts.eclipse.ide.jsdt.ui.IContextMenuConstants;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.jsdt.ui.actions.TypeScriptActionConstants;
import ts.eclipse.ide.ui.preferences.StatusInfo;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.ITypeScriptFile;
import ts.utils.CompletableFutureUtils;

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

	private CompletableFuture<List<ApplicableRefactorInfo>> refactorInfosPromise;

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

	private int fillRefactorMenu(final IMenuManager refactorSubmenu) {
		int added = 0;
		refactorSubmenu.add(new Separator(GROUP_REORG));
		added += addAction(refactorSubmenu, fRenameAction);

		ITextSelection textSelection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		try {
			ITypeScriptFile tsFile = fEditor.getTypeScriptFile();
			if (tsFile.getProject().canSupport(CommandNames.GetApplicableRefactors)) {
				// TypeScript >= 2.4.1 : support for applicable refactors

				final Menu menu = ((MenuManager) refactorSubmenu).getMenu();
				
				int startPosition = textSelection.getOffset();
				Integer endPosition = textSelection.getOffset() + textSelection.getLength();
				CompletableFutureUtils.cancel(refactorInfosPromise);
				refactorInfosPromise = tsFile.getApplicableRefactors(startPosition, endPosition);
				refactorInfosPromise.whenComplete(new BiConsumer<List<ApplicableRefactorInfo>, Throwable>() {

					@Override
					public void accept(List<ApplicableRefactorInfo> infos, Throwable u) {						
						UIJob job = new UIJob(menu.getDisplay(), "refactoring menu job") {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								// if (u != null) {
								// // log?
								// item.setText(u.getMessage());
								// } else {
								// for (CodeLens lens : t) {
								// if (lens != null) {
								// final MenuItem item = new MenuItem(menu, SWT.NONE, index);
								// item.setText(lens.getCommand().getTitle());
								// item.setEnabled(false);
								// }
								// }
								// }
								if (infos != null) {
									for (ApplicableRefactorInfo info : infos) {
										if (info.isInlineable()) {
											for (RefactorActionInfo action : info.getActions()) {
												addMenu(info.getDescription(), info.getName(), action.getName(),
														startPosition, endPosition, tsFile, menu);
											}
										} else {
											// TODO: support no inlineable

										}
									}
								}
								return Status.OK_STATUS;
							}

							private void addMenu(String title, String refactor, String action, int startPosition,
									Integer endPosition, ITypeScriptFile tsFile, Menu menu) {
								final MenuItem item = new MenuItem(menu, SWT.NONE);
								item.setText(title);
								item.setEnabled(true);

								item.addSelectionListener(new SelectionListener() {

									@Override
									public void widgetSelected(SelectionEvent e) {
										try {
											RefactorEditInfo info = tsFile
													.getEditsForRefactor(startPosition, endPosition, refactor, action)
													.get(5000, TimeUnit.MILLISECONDS);
											EditorUtils.applyEdit(info.getEdits(),
													((IIDETypeScriptFile) tsFile).getDocument(), tsFile.getName());
										} catch (Exception ex) {
											ErrorDialog.openError(menu.getShell(), "Refactoring Error",
													"Error while applying refactoring",
													new StatusInfo(IStatus.ERROR, ex.getMessage()));
										}
									}

									@Override
									public void widgetDefaultSelected(SelectionEvent e) {

									}
								});
							}
						};
						job.schedule();
					}
				});

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
