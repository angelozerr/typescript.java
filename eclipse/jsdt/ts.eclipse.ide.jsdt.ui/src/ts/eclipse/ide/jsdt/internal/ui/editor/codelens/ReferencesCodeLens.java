package ts.eclipse.ide.jsdt.internal.ui.editor.codelens;

import org.eclipse.jface.text.provisional.codelens.CodeLens;
import org.eclipse.jface.text.provisional.codelens.Range;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;

public class ReferencesCodeLens extends CodeLens {

	private final IIDETypeScriptFile tsFile;

	public ReferencesCodeLens(IIDETypeScriptFile tsFile, Range range) {
		super(range);
		this.tsFile = tsFile;
	}

	public IIDETypeScriptFile getTsFile() {
		return tsFile;
	}

}
