package ts.eclipse.ide.jsdt.internal.ui.editor.format;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import ts.TypeScriptException;
import ts.client.format.ITypeScriptFormatCollector;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;

/**
 * Content formatter which consumes tsserver "format" command to format an
 * IDocument.
 * 
 * @author azerr
 *
 */
public class TypeScriptContentFormatter implements IContentFormatter {

	private final IResource resource;
	private DocumentRewriteSession fRewriteSession;

	public TypeScriptContentFormatter(IResource resource) {
		this.resource = resource;
	}

	@Override
	public void format(IDocument document, IRegion region) {
		try {
			IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getDefault()
					.getTypeScriptProject(resource.getProject());
			final IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);

			final MultiTextEdit textEdit = new MultiTextEdit();
			int startPosition = region.getOffset();
			int endPosition = region.getOffset() + region.getLength() - 1;
			tsFile.format(startPosition, endPosition, new ITypeScriptFormatCollector() {

				@Override
				public void format(int startLine, int startOffset, int endLine, int endOffset, String newText)
						throws TypeScriptException {
					int start = tsFile.getPosition(startLine, startOffset);
					int end = tsFile.getPosition(endLine, endOffset);
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
			});
			textEdit.apply(document, TextEdit.CREATE_UNDO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		return null;
	}

}
