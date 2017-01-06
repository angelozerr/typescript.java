package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.jface.text.contentassist.TypeScriptCompletionProposalWithExtension7;

public class JSDTTypeScriptCompletionProposalWithExtension7 extends TypeScriptCompletionProposalWithExtension7
		implements IJavaCompletionProposal {

	public JSDTTypeScriptCompletionProposalWithExtension7(ICompletionEntryMatcher matcher, String fileName, int line,
			int offset, ITypeScriptServiceClient client, int position, String prefix) {
		super(matcher, fileName, line, offset, client, position, prefix);
	}

	@Override
	protected Shell getActiveWorkbenchShell() {
		return JSDTTypeScriptUIPlugin.getActiveWorkbenchShell();
	}

}
