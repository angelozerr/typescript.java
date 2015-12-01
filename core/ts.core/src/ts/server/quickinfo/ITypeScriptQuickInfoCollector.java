package ts.server.quickinfo;

import ts.server.ITypeScriptCollector;

public interface ITypeScriptQuickInfoCollector extends ITypeScriptCollector {

	void setInfo(String kind, String kindModifiers, int startLine, int startOffset, int endLine, int endOffset,
			String displayString, String documentation);

}
