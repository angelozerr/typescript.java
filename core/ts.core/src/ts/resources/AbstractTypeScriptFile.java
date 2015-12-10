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
		ITypeScriptServiceClient client = tsProject.getClient();
		((TypeScriptProject) tsProject).synchFileContent(this, client);
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		String prefix = null;
		client.completions(this.getName(), line, offset, prefix, collector);
	}

	@Override
	public void definition(int position, ITypeScriptDefinitionCollector collector) throws TSException {
		ITypeScriptServiceClient client = tsProject.getClient();
		((TypeScriptProject) tsProject).synchFileContent(this, client);
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.definition(this.getName(), line, offset, collector);
	}
}
