package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import ts.LineOffset;
import ts.TSException;
import ts.resources.AbstractTypeScriptFile;

public class IDETypeScriptFile extends AbstractTypeScriptFile implements IDocumentListener {

	private final IDocument document;

	public IDETypeScriptFile(IResource file, IDocument document) {
		super(getFileName(file));
		this.document = document;
		this.document.addDocumentListener(this);
	}

	public static String getFileName(IResource file) {
		return file.getProjectRelativePath().toString();
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		setDirty(true);
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		setDirty(true);
	}

	@Override
	public void dispose() {
		this.document.removeDocumentListener(this);
	}

	@Override
	public String getPrefix(int position) {
		return null; // TSHelper.getPrefix(getContents(), position);
	}

	@Override
	public LineOffset getLineOffset(int position) throws TSException {
		try {
			int line = document.getLineOfOffset(position);
			int offset = position - document.getLineOffset(line);
			return new LineOffset(line + 1, offset + 1);
		} catch (BadLocationException e) {
			throw new TSException(e);
		}
	}

	@Override
	public String getContents() {
		return document.get();
	}
}
