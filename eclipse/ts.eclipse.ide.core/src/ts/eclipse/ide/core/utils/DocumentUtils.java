package ts.eclipse.ide.core.utils;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import ts.TypeScriptException;
import ts.client.CodeEdit;
import ts.client.Location;

public class DocumentUtils {

	public static int getPosition(IDocument document, Location location) throws TypeScriptException {
		return getPosition(document, location.getLine(), location.getOffset());
	}

	public static int getPosition(IDocument document, int line, int offset) throws TypeScriptException {
		if (document == null) {
			throw new TypeScriptException("document cannot be null");
		}
		try {
			return document.getLineOffset(line - 1) + offset - 1;
		} catch (BadLocationException e) {
			throw new TypeScriptException(e);
		}
	}

	public static TextEdit toTextEdit(CodeEdit codeEdit, IDocument document) throws TypeScriptException {
		MultiTextEdit textEdit = new MultiTextEdit();
		toTextEdit(codeEdit, document, textEdit);
		return textEdit;
	}

	public static TextEdit toTextEdit(List<CodeEdit> codeEdits, IDocument document) throws TypeScriptException {
		MultiTextEdit textEdit = new MultiTextEdit();
		for (CodeEdit codeEdit : codeEdits) {
			toTextEdit(codeEdit, document, textEdit);
		}
		return textEdit;
	}

	private static void toTextEdit(CodeEdit codeEdit, IDocument document, MultiTextEdit textEdit)
			throws TypeScriptException {
		String newText = codeEdit.getNewText();
		int startLine = codeEdit.getStart().getLine();
		int startOffset = codeEdit.getStart().getOffset();
		int endLine = codeEdit.getEnd().getLine();
		int endOffset = codeEdit.getEnd().getOffset();
		int start = DocumentUtils.getPosition(document, startLine, startOffset);
		int end = DocumentUtils.getPosition(document, endLine, endOffset);
		int length = end - start;
		if (newText.isEmpty()) {
			if (length > 0) {
				textEdit.addChild(new DeleteEdit(start, length));
			}
		} else {
			if (length > 0) {
				textEdit.addChild(new ReplaceEdit(start, length, newText));
			} else if (length == 0) {
				textEdit.addChild(new InsertEdit(start, newText));
			}
		}
	}

}
