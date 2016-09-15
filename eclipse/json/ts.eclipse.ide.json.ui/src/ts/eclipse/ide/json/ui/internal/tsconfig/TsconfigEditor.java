package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.ui.PartInitException;

import ts.eclipse.ide.json.ui.internal.AbstractFormEditor;

public class TsconfigEditor extends AbstractFormEditor {

	@Override
	protected void doAddPages() throws PartInitException {
		addPage(new OverviewPage(this));
	}
}
