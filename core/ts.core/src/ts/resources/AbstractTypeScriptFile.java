package ts.resources;

import ts.LineOffset;
import ts.TSException;
import ts.internal.LineOffsetReader;

public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final String name;
	private boolean dirty;

	public AbstractTypeScriptFile(String name) {
		this.name = name;
		this.setDirty(true);
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
	public LineOffset getLineOffset(int position) throws TSException {
		return new LineOffsetReader(getContents(), position).getLineOffset();
	}
}
