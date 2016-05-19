package ts.eclipse.ide.internal.core.tslint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import ts.TypeScriptException;
import ts.cmd.tslint.TSLintOptions;
import ts.cmd.tslint.TslintFormat;
import ts.cmd.tslint.TslintSettingsStrategy;
import ts.cmd.tslint.TypeScriptLint;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.tslint.IIDETypeScriptLint;

public class IDETypeScriptLint extends TypeScriptLint implements IIDETypeScriptLint {

	public IDETypeScriptLint(File tslintFile, File tslintJsonFile, File nodejsFile) {
		super(tslintFile, tslintJsonFile, nodejsFile);
	}

	@Override
	public void lint(IDETsconfigJson tsconfig, List<IFile> tsFiles, IIDETypeScriptProjectSettings projectSettings)
			throws TypeScriptException {
		TslintSettingsStrategy strategy = projectSettings.getTslintStrategy();
		switch (strategy) {
		case DisableTslint:
			return;
		case UseDefaultTslintJson:
			lint(tsconfig, tsFiles, null, false);
		case SearchForTslintJson:
			lint(tsconfig, tsFiles, null, true);
			return;
		case UseCustomTslintJson:
			File tslintJsonFile = super.getTslintJsonFile();
			lint(tsconfig, tsFiles, tslintJsonFile, false);
		}
	}

	private void lint(IDETsconfigJson tsconfig, List<IFile> tsFiles, File tslintJsonFile, boolean searchTslintJson)
			throws TypeScriptException {
		TSLintReporter reporter = new TSLintReporter();
		TSLintOptions options = new TSLintOptions();
		options.setFormat(TslintFormat.json);
		options.setConfig(tslintJsonFile);

		if (searchTslintJson) {
			// TODO
			Map<File, List<String>> i;
		}
		
		List<String> tsFileNames = new ArrayList<String>();
		for (IFile tsFile : tsFiles) {
			// add to the list file names
			tsFileNames.add(tsFile.getLocation().toString());
		}

		IProject project = tsconfig.getTsconfigFile().getProject();
		super.execute(project.getLocation().toFile(), options, tsFileNames, reporter);
	}

}
