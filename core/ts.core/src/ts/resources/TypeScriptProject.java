package ts.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ts.LineOffset;
import ts.TSException;
import ts.server.ITypeScriptServiceClient;
import ts.server.ITypeScriptServiceClientFactory;
import ts.server.collectors.ICompletionCollector;

public class TypeScriptProject implements ITypeScriptProject, ITypeScriptServiceClientFactory {

	private final File projectDir;
	private final ITypeScriptServiceClientFactory factory;
	private final Map<String, ITypeScriptFile> openedFiles;
	private ITypeScriptServiceClient client;

	/**
	 * Tern project constructor.
	 * 
	 * @param projectDir
	 *            the project base directory.
	 */
	public TypeScriptProject(File projectDir, ITypeScriptServiceClientFactory factory) {
		this.projectDir = projectDir;
		this.factory = factory;
		this.openedFiles = new HashMap<String, ITypeScriptFile>();
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
		this.openedFiles.put(file.getName(), file);
	}

	@Override
	public void closeFile(String fileName) throws TSException {
		getClient().closeFile(fileName);
		this.openedFiles.remove(fileName);
	}

	@Override
	public void completions(ITypeScriptFile file, int position, ICompletionCollector collector) throws TSException {
		ITypeScriptServiceClient client = getClient();
		synchFileContent(file, client);
		LineOffset lineOffset = file.getLineOffset(position);
		int line = lineOffset.getLine();
		int offset = lineOffset.getOffset();
		String prefix = null;
		client.completions(file.getName(), line, offset, prefix, collector);
	}

	@Override
	public void changeFile(ITypeScriptFile file, int start, int end, String newText) throws TSException {
		LineOffset lineOffset = file.getLineOffset(start);
		int line = lineOffset.getLine();
		int offset = lineOffset.getOffset();
		LineOffset endLineOffset = file.getLineOffset(end);
		int endLine = endLineOffset.getLine();
		int endOffset = endLineOffset.getOffset();
		getClient().changeFile(file.getName(), line, offset, endLine, endOffset, newText);
	}

	private void synchFileContent(ITypeScriptFile file, ITypeScriptServiceClient client) throws TSException {
		if (file.isDirty()) {
			client.updateFile(file.getName(), file.getContents());
			file.setDirty(false);
		}
	}

	@Override
	public final ITypeScriptServiceClient getClient() throws TSException {
		if (client == null) {
			this.client = create(getProjectDir());
		}
		return client;
	}

	@Override
	public ITypeScriptFile getOpenedFile(String fileName) {
		return openedFiles.get(fileName);
	}

	@Override
	public void dispose() throws TSException {
		for (ITypeScriptFile openedFile : openedFiles.values()) {
			closeFile(openedFile.getName());
		}
		if (client != null) {
			client.dispose();
		}
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) throws TSException {
		return factory.create(projectDir);
	}
}
