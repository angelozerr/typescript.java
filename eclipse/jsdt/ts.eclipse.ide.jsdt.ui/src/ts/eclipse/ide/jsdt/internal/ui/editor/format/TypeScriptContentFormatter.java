package ts.eclipse.ide.jsdt.internal.ui.editor.format;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.ui.texteditor.ITextEditor;

public class TypeScriptContentFormatter implements IContentFormatter {

	public TypeScriptContentFormatter(ITextEditor editor) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void format(IDocument document, IRegion region) {
		System.err.println(document.get());
	}

	@Override
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		return null;
	}

}
