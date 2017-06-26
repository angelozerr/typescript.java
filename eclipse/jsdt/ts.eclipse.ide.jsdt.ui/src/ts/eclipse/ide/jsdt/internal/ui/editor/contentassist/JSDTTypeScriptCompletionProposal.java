package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.jface.text.contentassist.TypeScriptCompletionProposal;

public class JSDTTypeScriptCompletionProposal extends TypeScriptCompletionProposal implements IJavaCompletionProposal {

	public JSDTTypeScriptCompletionProposal(ICompletionEntryMatcher matcher, String fileName, int line, int offset,
			ITypeScriptServiceClient client, int position, String prefix, ITextViewer textViewer) {
		super(matcher, fileName, line, offset, client, position, prefix, textViewer);
	}

	@Override
	protected Shell getActiveWorkbenchShell() {
		return JSDTTypeScriptUIPlugin.getActiveWorkbenchShell();
	}

}
