package ts.eclipse.ide.jsdt.internal.ui.editor.codelens;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.provisional.codelens.CodeLens;
import org.eclipse.jface.text.provisional.codelens.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.jsdt.internal.ui.search.SearchUtil;

import ts.TypeScriptException;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.ui.implementation.TypeScriptImplementationDialog;
import ts.eclipse.ide.ui.search.TypeScriptSearchQuery;

public class ReferencesCodeLens extends CodeLens {
	private final IIDETypeScriptFile tsFile;

	public ReferencesCodeLens(IIDETypeScriptFile tsFile, Range range) {
		super(range);
		this.tsFile = tsFile;
	}

	public IIDETypeScriptFile getTsFile() {
		return tsFile;
	}

	@Override
	public void open() {
		String command = getCommand().getCommand();
		if (command.equals("references")) {
			// Execute Search
			try {
				int offset = tsFile.getPosition(getRange().startLineNumber, getRange().startColumn);
				TypeScriptSearchQuery query = new TypeScriptSearchQuery(tsFile.getResource(), offset);
				SearchUtil.runQueryInBackground(query);
			} catch (TypeScriptException e) {
				e.printStackTrace();
			}
		} else {
			// Open Implementation dialog
			Display.getDefault().asyncExec(() -> {
				try {
					Shell parent = Display.getDefault().getActiveShell();
					TypeScriptImplementationDialog dialog = new TypeScriptImplementationDialog(parent, SWT.RESIZE,
							tsFile);
					int offset = tsFile.getPosition(getRange().startLineNumber, getRange().startColumn);
					ITextSelection selection = new TextSelection(offset, 1);
					dialog.setSize(450, 500);
					dialog.setInput(selection);
					dialog.open();
				} catch (TypeScriptException e) {
					e.printStackTrace();
				}
			});

		}
	}

}
