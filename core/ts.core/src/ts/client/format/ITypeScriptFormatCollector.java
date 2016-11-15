package ts.client.format;

import java.util.List;

import ts.TypeScriptException;
import ts.client.CodeEdit;
import ts.client.ITypeScriptCollector;

public interface ITypeScriptFormatCollector extends ITypeScriptCollector {

	void format(List<CodeEdit> codeEdits) throws TypeScriptException;
}
