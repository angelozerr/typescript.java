package ts.npm;

import java.io.IOException;
import java.util.List;

import ts.OS;

public class NPMModule {

	private final String moduleName;
	private final OS os;
	private List<String> versions;

	NPMModule(String moduleName, OS os) {
		this.moduleName = moduleName;
		this.os = os;
	}

	public List<String> getAvailableVersions() throws IOException {
		if (versions == null) {
			versions = NPMHelper.getVersions(moduleName, os);
		}
		return versions;
	}

}
