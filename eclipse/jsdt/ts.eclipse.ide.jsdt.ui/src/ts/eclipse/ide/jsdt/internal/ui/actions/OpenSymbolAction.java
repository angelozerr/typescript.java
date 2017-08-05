package ts.eclipse.ide.jsdt.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.jsdt.internal.ui.JavaPluginImages;
import org.eclipse.wst.jsdt.internal.ui.JavaUIMessages;

import ts.client.navto.NavtoItem;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.dialogs.OpenSymbolSelectionDialog;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.ITypeScriptFile;

public class OpenSymbolAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

	public OpenSymbolAction() {
		super();
		setText(JavaUIMessages.OpenTypeAction_label);
		setDescription(JavaUIMessages.OpenTypeAction_description);
		setToolTipText(JavaUIMessages.OpenTypeAction_tooltip);
		setImageDescriptor(JavaPluginImages.DESC_TOOL_OPENTYPE);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IJavaHelpContextIds.OPEN_TYPE_ACTION);
	}

	// ---- IWorkbenchWindowActionDelegate
	// ------------------------------------------------

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void dispose() {
		// do nothing.
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// do nothing.
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing. Action doesn't depend on selection.
	}

	// ---- IActionDelegate2
	// ------------------------------------------------

	@Override
	public void runWithEvent(IAction action, Event event) {
		runWithEvent(event);
	}

	@Override
	public void init(IAction action) {
		// do nothing.
	}

	@Override
	public void runWithEvent(Event event) {
		Shell parent = TypeScriptUIPlugin.getActiveWorkbenchShell();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				if (page.getActiveEditor() instanceof TypeScriptEditor) {
					ITypeScriptFile tsFile = ((TypeScriptEditor) page.getActiveEditor()).getTypeScriptFile();
					OpenSymbolSelectionDialog dialog = new OpenSymbolSelectionDialog(tsFile, parent, true);
					int result = dialog.open();
					if (result == 0) {
						Object[] resources = dialog.getResult();
						if (resources != null && resources.length > 0) {
							NavtoItem item = (NavtoItem) resources[0];
							EditorUtils.openInEditor(item);
						}
					}
				}
			}
		}

	}

}
