package ts.client.diagnostics;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptDiagnosticsCollector extends ITypeScriptCollector {

	void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
			int endOffset, String category, int code);

}
