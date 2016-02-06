package ts.core.tests;

import java.io.File;

import ts.TypeScriptException;
import ts.resources.SynchStrategy;
import ts.resources.TypeScriptProject;
import ts.server.ITypeScriptServiceClient;
import ts.server.TypeScriptServiceClient;

public class MockTypeScriptProject extends TypeScriptProject {

	public MockTypeScriptProject(File projectDir) {
		super(projectDir, null, SynchStrategy.CHANGE);
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) throws TypeScriptException {
		return new TypeScriptServiceClient(projectDir,
				new File("../ts.repository/node_modules/typescript/bin/tsserver"), null);
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
