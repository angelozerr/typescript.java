package ts.core.tests;

import java.io.File;
import java.util.Arrays;

import ts.TypeScriptException;
import ts.client.TypeScriptServiceClient;
import ts.utils.VersionHelper;
import ts.utils.ZipUtils;

public class TypeScriptServiceClientFactory {

	public static TypeScriptServiceClient create(final File projectDir) throws TypeScriptException {
		File repositoriesDir = new File("../ts.repository/repositories");

		File lastRepositoryZip = Arrays.stream(repositoriesDir.listFiles())
				.max((f1, f2) -> VersionHelper.versionCompare(f1.getName(), f2.getName())).get();

		try {
			String name = lastRepositoryZip.getName().replaceFirst(".zip", "");
			File repositoryDir = new File(System.getProperty("java.io.tmpdir"), "typescript.java");
			File tsserverFile = new File(repositoryDir, name + "/node_modules/typescript/bin/tsserver");
			if (!repositoryDir.exists()) {
				ZipUtils.extractZip(lastRepositoryZip, repositoryDir);
			}
			File nodeFile = null;
			return new TypeScriptServiceClient(projectDir, tsserverFile, nodeFile);
		} catch (TypeScriptException e) {
			throw e;
		} catch (Exception e) {
			throw new TypeScriptException(e);
		}
	}
}
