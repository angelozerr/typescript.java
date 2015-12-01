package ts.server.geterr;

import ts.server.ITypeScriptCollector;

public interface ITypeScriptGeterrCollector extends ITypeScriptCollector {

	void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
			int endOffset);

}
