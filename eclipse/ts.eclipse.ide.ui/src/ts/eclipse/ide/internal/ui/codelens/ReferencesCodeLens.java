package ts.eclipse.ide.internal.ui.codelens;

import org.eclipse.jface.text.provisional.codelens.CodeLens;
import org.eclipse.jface.text.provisional.codelens.Range;
import org.eclipse.search.ui.NewSearchUI;

import ts.TypeScriptException;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
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
		// Execute Search
		try {
			int offset = tsFile.getPosition(getRange().startLineNumber, getRange().startColumn);
			TypeScriptSearchQuery query = new TypeScriptSearchQuery(tsFile.getResource(), offset);
			NewSearchUI.runQueryInBackground(query);
		} catch (TypeScriptException e) {
			e.printStackTrace();
		}
	}

}
