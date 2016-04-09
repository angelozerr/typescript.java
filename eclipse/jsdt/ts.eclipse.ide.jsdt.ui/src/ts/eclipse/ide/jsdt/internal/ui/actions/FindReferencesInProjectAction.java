package ts.eclipse.ide.jsdt.internal.ui.actions;

import org.eclipse.ui.IWorkbenchSite;

import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;

public class FindReferencesInProjectAction extends FindAction {

	FindReferencesInProjectAction(TypeScriptEditor editor) {
		super(editor);
	}

	public FindReferencesInProjectAction(IWorkbenchSite site) {
		super(site);
	}

	@Override
	void init() {
		// TODO Auto-generated method stub

	}

	@Override
	Class[] getValidTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	int getLimitTo() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		System.err.println("TODO");
		super.run();
	}
}
