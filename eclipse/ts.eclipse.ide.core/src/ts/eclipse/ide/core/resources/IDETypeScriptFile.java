package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import ts.TSException;
import ts.resources.ITypeScriptFile;

public class IDETypeScriptFile implements ITypeScriptFile, IDocumentListener {

	private final IDocument document;
	private final IResource file;
	private boolean dirty;

	public IDETypeScriptFile(IResource file, IDocument document) {
		this.file = file;
		this.document = document;
		this.dirty = true;
		this.document.addDocumentListener(this);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public String getName() {
		return getFileName(file);
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
		return null;
	}

	@Override
	public int getOffset(int position) throws TSException {
		try {
			int line = document.getLineOfOffset(position);
			return position - document.getLineOffset(line) + 1;
		} catch (BadLocationException e) {
			throw new TSException(e);
		}
	}

	@Override
	public int getLine(int position) throws TSException {
		try {
			return document.getLineOfOffset(position) + 1;
		} catch (BadLocationException e) {
			throw new TSException(e);
		}
	}

	@Override
	public String getContents() {
		return document.get();
	}
}
