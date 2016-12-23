package ts.eclipse.ide.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.eclipse.ide.ui.utils.HTMLTypeScriptPrinter;

public class HTMLTypeScriptQuickInfoCollector implements ITypeScriptQuickInfoCollector {

	private final IFile tsFile;
	private String html;

	public HTMLTypeScriptQuickInfoCollector(IFile tsFile) {
		this.tsFile = tsFile;;
	}

	@Override
	public void setInfo(String kind, String kindModifiers, int startLine, int startOffset, int endLine, int endOffset,
			String displayString, String documentation) {
		this.html = HTMLTypeScriptPrinter.getQuickInfo(kind, kindModifiers, displayString, documentation, tsFile);
	}

	public String getInfo() {
		return html;
	}

}
