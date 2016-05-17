package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.jface.text.contentassist.TypeScriptCompletionProposal;

public class JSDTTypeScriptCompletionProposal extends TypeScriptCompletionProposal implements IJavaCompletionProposal {

	public JSDTTypeScriptCompletionProposal(String name, String kind, String kindModifiers, String sortText,
			int position, String prefix, String fileName, int line, int offset, ICompletionEntryMatcher matcher,
			ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, position, prefix, fileName, line, offset, matcher, client);
	}

}
