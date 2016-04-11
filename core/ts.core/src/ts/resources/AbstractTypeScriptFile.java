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
package ts.resources;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.Location;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.client.format.ITypeScriptFormatCollector;
import ts.client.references.ITypeScriptReferencesCollector;
import ts.internal.LocationReader;

/**
 * Abstract TypeScript file.
 *
 */
public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final ITypeScriptProject tsProject;
	private boolean dirty;
	protected final Object synchLock = new Object();
	private boolean opened;

	public AbstractTypeScriptFile(ITypeScriptProject tsProject) {
		this.tsProject = tsProject;
		this.setDirty(false);
	}

	@Override
	public ITypeScriptProject getProject() {
		return tsProject;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Location getLocation(int position) throws TypeScriptException {
		return new LocationReader(getContents(), position).getLineOffset();
	}

	@Override
	public int getPosition(int line, int offset) throws TypeScriptException {
		// TODO: implement that
		throw new UnsupportedOperationException();
	}

	@Override
	public void open() throws TypeScriptException {
		((TypeScriptProject) tsProject).openFile(this);
		this.opened = true;
	}

	@Override
	public void close() throws TypeScriptException {
		((TypeScriptProject) tsProject).closeFile(this);
		this.opened = false;
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	void setOpened(boolean opened) {
		this.opened = opened;
	}

	@Override
	public void completions(int position, ITypeScriptCompletionCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		String prefix = null;
		client.completions(this.getName(), line, offset, prefix, collector);
	}

	@Override
	public void definition(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.definition(this.getName(), line, offset, collector);
	}

	@Override
	public void format(int startPosition, int endPosition, ITypeScriptFormatCollector collector)
			throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location start = this.getLocation(startPosition);
		Location end = this.getLocation(endPosition);
		client.format(this.getName(), start.getLine(), start.getOffset(), end.getLine(), end.getOffset(), collector);
	}

	@Override
	public void references(int position, ITypeScriptReferencesCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.references(this.getName(), line, offset, collector);
	}

	@Override
	public synchronized void synch() throws TypeScriptException {
		if (!isDirty()) {
			// no need to synchronize the file content with tsserver.
			return;
		}
		switch (tsProject.getProjectSettings().getSynchStrategy()) {
		case RELOAD:
			// reload strategy : store the content of the ts file in a temporary
			// file and call reload command.
			tsProject.getClient().updateFile(this.getName(), this.getContents());
			setDirty(false);
			break;
		case CHANGE:
			// change strategy: wait until "change" command is not finished.
			while (isDirty()) {
				try {
					synchLock.wait(5);
				} catch (InterruptedException e) {
					throw new TypeScriptException(e);
				}
			}
			break;
		}

	}
}
