package ts.eclipse.ide.jsdt.internal.ui.editor.format;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import ts.TypeScriptException;
import ts.client.CodeEdit;
import ts.client.format.ITypeScriptFormatCollector;
import ts.eclipse.ide.core.utils.DocumentUtils;

class TypeScriptFormatCollector implements ITypeScriptFormatCollector {

	private final IDocument document;
	private TextEdit textEdit;

	public TypeScriptFormatCollector(IDocument document) {
		this.document = document;
	}

	@Override
	public void format(List<CodeEdit> codeEdits) throws TypeScriptException {
		this.textEdit = DocumentUtils.toTextEdit(codeEdits, document);
	}

	public TextEdit getTextEdit() {
		return textEdit;
	}
}