package ts.resources;

import ts.Location;
import ts.TSException;
import ts.internal.LocationReader;

public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final ITypeScriptProject tsProject;
	private boolean dirty;

	public AbstractTypeScriptFile(ITypeScriptProject tsProject) {
		this.tsProject = tsProject;
		this.setDirty(false);
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
}
