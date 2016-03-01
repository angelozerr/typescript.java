package ts.core.tests;

import java.io.File;

import ts.TypeScriptException;
import ts.repository.TypeScriptRepositoryException;
import ts.resources.BasicTypeScriptProjectSettings;
import ts.resources.TypeScriptProject;

public class MockTypeScriptProject extends TypeScriptProject {

	public MockTypeScriptProject(File projectDir) throws TypeScriptRepositoryException {
		super(projectDir, new BasicTypeScriptProjectSettings(null, new File("../ts.repository/node_modules")));
	}

	public synchronized MockTypeScriptFile openFile(File file, boolean normalize) throws TypeScriptException {
		String fileName = MockTypeScriptFile.getFileName(file, normalize);
		MockTypeScriptFile tsFile = (MockTypeScriptFile) super.getOpenedFile(fileName);
		if (tsFile == null) {
			tsFile = new MockTypeScriptFile(file, this, normalize);
		}
		if (!tsFile.isOpened()) {
			tsFile.open();
		}
		return tsFile;
	}

}
