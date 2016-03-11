/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import ts.TypeScriptException;
import ts.client.Location;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.resources.AbstractTypeScriptFile;
import ts.resources.SynchStrategy;
import ts.utils.FileUtils;

/**
 * {@link IIDETypeScriptFile} implementation.
 *
 */
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
		return FileUtils.normalizeSlashes(file.getLocation().toString());
	}

	@Override
	public String getName() {
		return getFileName(file);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		setDirty(true);
		if (getProject().getProjectSettings().getSynchStrategy() == SynchStrategy.CHANGE) {
			synchronized (synchLock) {
				try {
					String newText = event.getText();
					int position = event.getOffset();

					Location loc = getLocation(position);
					int line = loc.getLine();
					int offset = loc.getOffset();

					Location endLoc = getLocation(position + event.getLength());
					int endLine = endLoc.getLine();
					int endOffset = endLoc.getOffset();

					getProject().getClient().changeFile(getName(), line, offset, endLine, endOffset, newText);
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					setDirty(false);
					synchLock.notifyAll();
				}
			}
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {

	}

	@Override
	public void close() throws TypeScriptException {
		this.document.removeDocumentListener(this);
		super.close();
	}

	@Override
	public String getPrefix(int position) {
		return null; // TSHelper.getPrefix(getContents(), position);
	}

	@Override
	public Location getLocation(int position) throws TypeScriptException {
		try {
			int line = document.getLineOfOffset(position);
			int offset = position - document.getLineOffset(line);
			return new Location(line + 1, offset + 1);
		} catch (BadLocationException e) {
			throw new TypeScriptException(e);
		}
	}

	@Override
	public String getContents() {
		return document.get();
	}

	@Override
	public int getPosition(int line, int offset) throws TypeScriptException {
		try {
			return document.getLineOffset(line - 1) + offset - 1;
		} catch (BadLocationException e) {
			throw new TypeScriptException(e);
		}
	}

	@Override
	public IResource getResource() {
		return file;
	}
}
