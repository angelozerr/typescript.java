package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.jface.text.contentassist.TypeScriptCompletionProposalWithExtension7;

public class JSDTTypeScriptCompletionProposalWithExtension7 extends TypeScriptCompletionProposalWithExtension7
		implements IJavaCompletionProposal {

	public JSDTTypeScriptCompletionProposalWithExtension7(String name, String kind, String kindModifiers,
			String sortText, int position, String prefix, String fileName, int line, int offset,
			ICompletionEntryMatcher matcher, ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, position, prefix, fileName, line, offset, matcher, client);
	}

	@Override
	protected Shell getActiveWorkbenchShell() {
		return JSDTTypeScriptUIPlugin.getActiveWorkbenchShell();
	}

}
