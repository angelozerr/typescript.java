package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import ts.Location;
import ts.TSException;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.resources.AbstractTypeScriptFile;

public class IDETypeScriptFile extends AbstractTypeScriptFile implements IIDETypeScriptFile, IDocumentListener {

	private final IResource file;
	private final IDocument document;

	public IDETypeScriptFile(IResource file, IDocument document, IIDETypeScriptProject tsProject) {
		super(tsProject);
		this.file = file;
		this.document = document;
		this.document.addDocumentListener(this);
	}

	public static String getFileName(IResource file) {
		return file.getLocation().toString();
	}

	@Override
	public String getName() {
		return getFileName(file);
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
	public Location getLocation(int position) throws TSException {
		try {
			int line = document.getLineOfOffset(position);
			int offset = position - document.getLineOffset(line);
			return new Location(line + 1, offset + 1);
		} catch (BadLocationException e) {
			throw new TSException(e);
		}
	}

	@Override
	public String getContents() {
		return document.get();
	}

	@Override
	public int getPosition(int line, int offset) throws TSException {
		try {
			return document.getLineOffset(line - 1) + offset - 1;
		} catch (BadLocationException e) {
			throw new TSException(e);
		}
	}

	@Override
	public IResource getResource() {
		return file;
	}
}
