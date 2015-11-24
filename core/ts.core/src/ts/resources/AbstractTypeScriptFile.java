package ts.resources;

import ts.Location;
import ts.TSException;
import ts.internal.LocationReader;

public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final String name;
	private boolean dirty;

	public AbstractTypeScriptFile(String name) {
		this.name = name;
		this.setDirty(false);
	}

	@Override
	public String getName() {
		return name;
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
}
