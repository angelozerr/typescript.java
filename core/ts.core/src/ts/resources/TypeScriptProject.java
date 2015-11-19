package ts.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ts.ICompletionInfo;
import ts.TSException;
import ts.server.ITypeScriptServiceClient;

public class TypeScriptProject implements ITypeScriptProject {

	private final File projectDir;
	private final Map<String, ITypeScriptFile> files;

	/**
	 * Tern project constructor.
	 * 
	 * @param projectDir
	 *            the project base directory.
	 */
	public TypeScriptProject(File projectDir) {
		this.projectDir = projectDir;
		this.files = new HashMap<String, ITypeScriptFile>();
	}

	/**
	 * Returns the project base directory.
	 * 
	 * @return the project base directory.
	 */
	public File getProjectDir() {
		return projectDir;
	}

	@Override
	public void openFile(ITypeScriptFile file) throws TSException {
		getClient().openFile(file.getName());
		this.files.put(file.getName(), file);
	}

	@Override
	public void closeFile(String fileName) throws TSException {
		getClient().closeFile(fileName);
		this.files.remove(fileName);
	}

	@Override
	public ICompletionInfo getCompletionsAtPosition(ITypeScriptFile file, int position) throws TSException {
		ITypeScriptServiceClient client = getClient();
		updateFileIfNeeded(file, client);
		int line = file.getLine(position);
		int offset = file.getOffset(position);
		String prefix = file.getPrefix(position);
		return client.getCompletionsAtLineOffset(file.getName(), line, offset, prefix);
	}

	private void updateFileIfNeeded(ITypeScriptFile file, ITypeScriptServiceClient client) throws TSException {
		if (file.isDirty()) {
			client.updateFile(file.getName(), file.getContents());
			file.setDirty(false);
		}
	}

	@Override
	public ITypeScriptServiceClient getClient() throws TSException {
		return null;
	}

	@Override
	public ITypeScriptFile getFile(String fileName) {
		return files.get(fileName);
	}
}
