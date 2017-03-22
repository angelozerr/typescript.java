package ts.npm;

import java.io.IOException;
import java.util.List;

import ts.OS;

public class NPMModule {

	private final String name;
	private final OS os;
	private List<String> versions;

	NPMModule(String name, OS os) {
		this.name = name;
		this.os = os;
	}

	public List<String> getAvailableVersions() throws IOException {
		if (!isLoaded()) {
			versions = NPMHelper.getVersions(name, os);
		}
		return versions;
	}

	public boolean isLoaded() {
		return versions != null;
	}

	public String getName() {
		return name;
	}

}
