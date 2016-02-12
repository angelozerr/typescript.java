package ts.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ts.TypeScriptException;
import ts.client.ITypeScriptClientListener;
import ts.client.ITypeScriptServiceClient;
import ts.client.ITypeScriptServiceClientFactory;
import ts.client.Location;
import ts.client.geterr.ITypeScriptGeterrCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;
import ts.compiler.ITypeScriptCompiler;
import ts.compiler.TypeScriptCompiler;

public class TypeScriptProject implements ITypeScriptProject, ITypeScriptServiceClientFactory {

	private final File projectDir;
	private final ITypeScriptServiceClientFactory factory;
	private final SynchStrategy synchStrategy;

	// TypeScript service client
	private ITypeScriptServiceClient client;
	private final Map<String, ITypeScriptFile> openedFiles;

	// TypeScript compiler
	private ITypeScriptCompiler compiler;
	
	private final Map<String, Object> data;
	private final List<ITypeScriptClientListener> listeners;
	protected final Object serverLock = new Object();

	public TypeScriptProject(File projectDir, ITypeScriptServiceClientFactory factory) {
		this(projectDir, factory, SynchStrategy.RELOAD);
	}

	/**
	 * Tern project constructor.
	 * 
	 * @param projectDir
	 *            the project base directory.
	 */
	public TypeScriptProject(File projectDir, ITypeScriptServiceClientFactory factory, SynchStrategy synchStrategy) {
		this.projectDir = projectDir;
		this.factory = factory;
		this.synchStrategy = synchStrategy;
		this.openedFiles = new HashMap<String, ITypeScriptFile>();
		this.data = new HashMap<String, Object>();
		this.listeners = new ArrayList<ITypeScriptClientListener>();
	}

	/**
	 * Returns the project base directory.
	 * 
	 * @return the project base directory.
	 */
	public File getProjectDir() {
		return projectDir;
	}

	void openFile(ITypeScriptFile tsFile) throws TypeScriptException {
		String name = tsFile.getName();
		String contents = tsFile.getContents();
		getClient().openFile(name, contents);
		this.openedFiles.put(name, tsFile);
	}

	void closeFile(ITypeScriptFile tsFile) throws TypeScriptException {
		closeFile(tsFile, true);
	}

	void closeFile(ITypeScriptFile tsFile, boolean updateCache) throws TypeScriptException {
		String name = tsFile.getName();
		getClient().closeFile(name);
		((AbstractTypeScriptFile) tsFile).setOpened(false);
		if (updateCache) {
			this.openedFiles.remove(name);
		}
	}

	@Override
	public void signatureHelp(ITypeScriptFile file, int position, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException {
		ITypeScriptServiceClient client = getClient();
		file.synch();
		Location location = file.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.signatureHelp(file.getName(), line, offset, collector);
	}

	@Override
	public void quickInfo(ITypeScriptFile file, int position, ITypeScriptQuickInfoCollector collector)
			throws TypeScriptException {
		ITypeScriptServiceClient client = getClient();
		file.synch();
		Location location = file.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.quickInfo(file.getName(), line, offset, collector);
	}

	@Override
	public void changeFile(ITypeScriptFile file, int start, int end, String newText) throws TypeScriptException {
		Location loc = file.getLocation(start);
		int line = loc.getLine();
		int offset = loc.getOffset();
		Location endLoc = file.getLocation(end);
		int endLine = endLoc.getLine();
		int endOffset = endLoc.getOffset();
		getClient().changeFile(file.getName(), line, offset, endLine, endOffset, newText);
	}

	@Override
	public void geterr(ITypeScriptFile file, int delay, ITypeScriptGeterrCollector collector) throws TypeScriptException {
		file.synch();
		getClient().geterr(new String[] { file.getName() }, delay, collector);
	}

	@Override
	public final ITypeScriptServiceClient getClient() throws TypeScriptException {
		synchronized (serverLock) {
			if (isServerDisposed()) {
				try {
					this.client = create(getProjectDir());
					copyListeners();
					onCreateClient(client);
				} catch (Exception e) {
					if (e instanceof TypeScriptException) {
						throw (TypeScriptException) e;
					}
					throw new TypeScriptException(e);
				}

			}
			return client;
		}
	}

	protected void onCreateClient(ITypeScriptServiceClient client) {

	}

	@Override
	public synchronized ITypeScriptFile getOpenedFile(String fileName) {
		return openedFiles.get(fileName);
	}

	@Override
	public void dispose() throws TypeScriptException {
		disposeServer();
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) throws TypeScriptException {
		return factory.create(projectDir);
	}

	// ----------------------- TypeScript server listeners.

	@Override
	public void addServerListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
		copyListeners();
	}

	@Override
	public void removeServerListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
		synchronized (serverLock) {
			if (hasClient()) {
				this.client.removeClientListener(listener);
			}
		}
	}

	protected boolean hasClient() {
		return client != null;
	}

	private void copyListeners() {
		synchronized (serverLock) {
			if (hasClient()) {
				for (ITypeScriptClientListener listener : listeners) {
					client.addClientListener(listener);
				}
			}
		}
	}

	@Override
	public void disposeServer() {
		synchronized (serverLock) {
			if (!isServerDisposed()) {
				if (hasClient()) {
					// close opened files
					for (ITypeScriptFile openedFile : openedFiles.values()) {
						try {
							closeFile(openedFile, false);
						} catch (TypeScriptException e) {
							e.printStackTrace();
						}
					}
					// // notify uploader that we are going to dispose the
					// server,
					// // so that it can finish gracefully
					// ((IDETernFileUploader) ((TernFileSynchronizer)
					// getFileSynchronizer()).getTernFileUploader())
					// .serverToBeDisposed();
					client.dispose();
					client = null;
				}
			}
		}
	}

	// private void closeFiles() {
	// for (ITypeScriptFile openedFile : openedFiles.values()) {
	// try {
	// openedFile.close();
	// } catch (TSException e) {
	// // ignore error
	// }
	// }
	// openedFiles.clear();
	// }

	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		synchronized (data) {
			return (T) data.get(key);
		}
	}

	public void setData(String key, Object value) {
		synchronized (data) {
			data.put(key, value);
		}
	}

	@Override
	public boolean isServerDisposed() {
		synchronized (serverLock) {
			return client == null || client.isDisposed();
		}
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return synchStrategy;
	}
	
	@Override
	public ITypeScriptCompiler getCompiler() throws TypeScriptException {
		if (compiler == null) {
			compiler = createCompiler();
		}
		return compiler;
	}

	protected ITypeScriptCompiler createCompiler() throws TypeScriptException {
		// TODO Auto-generated method stub
		return null;
	}
}
