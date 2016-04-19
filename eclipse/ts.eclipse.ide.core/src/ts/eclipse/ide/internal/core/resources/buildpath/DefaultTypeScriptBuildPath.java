package ts.eclipse.ide.internal.core.resources.buildpath;

public class DefaultTypeScriptBuildPath extends TypeScriptBuildPath {

	public DefaultTypeScriptBuildPath() {
		super(null);
		addEntry(new TypeScriptBuildPathEntry("/", "", "node_modules/**"));
		addEntry(new TypeScriptBuildPathEntry("/src", "", "node_modules/**"));
	}

}
