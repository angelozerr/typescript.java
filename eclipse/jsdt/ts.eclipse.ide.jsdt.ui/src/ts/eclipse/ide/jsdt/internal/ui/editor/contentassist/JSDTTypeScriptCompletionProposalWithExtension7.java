package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import java.util.List;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntryDetails;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;
import ts.eclipse.ide.ui.utils.HTMLTypeScriptPrinter;
import ts.eclipse.jface.text.contentassist.TypeScriptCompletionProposalWithExtension7;

public class JSDTTypeScriptCompletionProposalWithExtension7 extends TypeScriptCompletionProposalWithExtension7
		implements IJavaCompletionProposal {

	public JSDTTypeScriptCompletionProposalWithExtension7(ICompletionEntryMatcher matcher, String fileName, int line,
			int offset, ITypeScriptServiceClient client, int position, String prefix, TextViewer textViewer) {
		super(matcher, fileName, line, offset, client, position, prefix, textViewer);
	}

	@Override
	protected Shell getActiveWorkbenchShell() {
		return JSDTTypeScriptUIPlugin.getActiveWorkbenchShell();
	}

	@Override
	protected String toHtml(List<CompletionEntryDetails> details) {
		boolean useTextMate = TypeScriptUIPlugin.getDefault().getPreferenceStore()
				.getBoolean(TypeScriptUIPreferenceConstants.USE_TEXMATE_FOR_SYNTAX_COLORING);
		return HTMLTypeScriptPrinter.getCompletionEntryDetail(details, getFileName(),
				useTextMate ? getOriginalTextViewer() : null);
	}
}
