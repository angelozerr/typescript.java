package ts.resources;

import java.io.File;

public class BasicTypeScriptProjectSettings implements ITypeScriptProjectSettings {

	private final File nodejsInstallPath;
	private final File tsserverFile;
	private final SynchStrategy synchStrategy;

	public BasicTypeScriptProjectSettings(File nodejsInstallPath, File tsserverFile) {
		this(nodejsInstallPath, tsserverFile, SynchStrategy.RELOAD);
	}

	public BasicTypeScriptProjectSettings(File nodejsInstallPath, File tsserverFile, SynchStrategy synchStrategy) {
		this.nodejsInstallPath = nodejsInstallPath;
		this.tsserverFile = tsserverFile;
		this.synchStrategy = synchStrategy;
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return synchStrategy;
	}

	@Override
	public File getNodejsInstallPath() {
		return nodejsInstallPath;
	}

	@Override
	public File getTsserverFile() {
		return tsserverFile;
	}

}
