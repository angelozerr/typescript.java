package ts.resources;

import ts.Location;
import ts.TSException;
import ts.internal.LocationReader;
import ts.server.ITypeScriptServiceClient;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;

public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final ITypeScriptProject tsProject;
	private boolean dirty;
	protected final Object synchLock = new Object();

	public AbstractTypeScriptFile(ITypeScriptProject tsProject) {
		this.tsProject = tsProject;
		this.setDirty(false);
	}

	@Override
	public ITypeScriptProject getProject() {
		return tsProject;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Location getLocation(int position) throws TSException {
		return new LocationReader(getContents(), position).getLineOffset();
	}

	@Override
	public int getPosition(int line, int offset) throws TSException {
		// TODO: implement that
		throw new UnsupportedOperationException();
	}

	@Override
	public void open() throws TSException {
		((TypeScriptProject) tsProject).openFile(this);
	}

	@Override
	public void close() throws TSException {
		((TypeScriptProject) tsProject).closeFile(this);
	}

	@Override
	public void completions(int position, ITypeScriptCompletionCollector collector) throws TSException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		String prefix = null;
		client.completions(this.getName(), line, offset, prefix, collector);
	}

	@Override
	public void definition(int position, ITypeScriptDefinitionCollector collector) throws TSException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.definition(this.getName(), line, offset, collector);
	}

	@Override
	public synchronized void synch() throws TSException {
		if (!isDirty()) {
			// no need to synchronize it.
			return;
		}
		switch (tsProject.getSynchStrategy()) {
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
					throw new TSException(e);
				}
			}
			break;
		}

	}
}
