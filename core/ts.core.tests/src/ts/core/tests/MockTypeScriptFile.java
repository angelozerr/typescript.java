package ts.core.tests;

import java.io.File;
import java.io.IOException;

import ts.client.Location;
import ts.resources.AbstractTypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.resources.SynchStrategy;
import ts.utils.FileUtils;

public class MockTypeScriptFile extends AbstractTypeScriptFile {

	private final File file;
	private final boolean normalize;
	private String contents;

	public MockTypeScriptFile(File file, ITypeScriptProject tsProject, boolean normalize) {
		super(tsProject);
		this.file = file;
		this.normalize = normalize;
		try {
			this.contents = FileUtils.getContents(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return getFileName(file, normalize);
	}

	public static String getFileName(File file, boolean normalize) {
		return FileUtils.getPath(file, normalize);
	}

	@Override
	public String getPrefix(int position) {
		return null;
	}

	@Override
	public String getContents() {
		return contents;
	}

	public void change(int position, int length, String newText) {
		setDirty(true);
		if (getProject().getProjectSettings().getSynchStrategy() == SynchStrategy.CHANGE) {
			synchronized (synchLock) {
				try {

					Location loc = getLocation(position);
					int line = loc.getLine();
					int offset = loc.getOffset();

					Location endLoc = getLocation(position + length);
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

}
