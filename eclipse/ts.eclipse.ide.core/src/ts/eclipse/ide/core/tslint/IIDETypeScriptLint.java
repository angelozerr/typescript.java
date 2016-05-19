package ts.eclipse.ide.core.tslint;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ts.TypeScriptException;
import ts.cmd.tslint.ITypeScriptLint;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;

public interface IIDETypeScriptLint extends ITypeScriptLint {

	void lint(IDETsconfigJson tsconfig, List<IFile> tsFiles, IIDETypeScriptProjectSettings projectSettings)
			throws TypeScriptException;

}
