package ts.client.codefixes;

import java.util.List;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptGetCodeFixesCollector extends ITypeScriptCollector {

	void fix(List<CodeAction> codeActions);
}
