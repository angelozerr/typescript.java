package ts.internal.resources;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ts.resources.ITypeScriptProject;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.resources.TypeScriptProject;
import ts.utils.FileUtils;

public class DefaultTypeScriptResourcesManager implements ITypeScriptResourcesManagerDelegate {

	private Map<String, ITypeScriptProject> projectCache = new HashMap<String, ITypeScriptProject>();

	@Override
	public ITypeScriptProject getTypeScriptProject(Object project, boolean force) throws IOException {
		if (!(project instanceof File)) {
			return null;
		}
		File projectDir = (File) project;
		if (!projectDir.exists()) {
			return null;
		}
		String path = projectDir.toString();
		try {
			path = projectDir.getCanonicalPath();
		} catch (Exception e) {
			// ignore
		}
		// cache projects for the particular path
		ITypeScriptProject result = projectCache.get(path);
		if (result == null) {
			result = new TypeScriptProject(projectDir, null);
			projectCache.put(path, result);
		}
		return result;
	}

	@Override
	public boolean isTsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.TS_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isJsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.JS_EXTENSION.equals(ext.toLowerCase());
	}
	
	protected String getExtension(Object fileObject) {
		if (fileObject instanceof File) {
			return FileUtils.getFileExtension(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.getFileExtension((String) fileObject);
		}
		return null;
	}

}
