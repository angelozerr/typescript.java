package ts.client.codefixes;

import java.util.List;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptGetSupportedCodeFixesCollector extends ITypeScriptCollector {

	void setSupportedCodeFixes(List<String> errorCodes);
}
