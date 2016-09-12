package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.wst.jsdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;

import ts.eclipse.ide.jsdt.ui.editor.contentassist.TypeScriptContentAssistInvocationContext;

public class TypeScriptCompletionProcessor extends JavaCompletionProcessor {

	private static final RelevanceSorter RELEVANCE_SORTER = new RelevanceSorter();
	
	private final ITextEditor editor;

	public TypeScriptCompletionProcessor(ITextEditor editor, ContentAssistant assistant, String partition) {
		super(editor, assistant, partition);
		this.editor = editor;
		assistant.setSorter(new ICompletionProposalSorter() {

			@Override
			public int compare(ICompletionProposal p1, ICompletionProposal p2) {
				return RELEVANCE_SORTER.compare(p1, p2);
			}
		});
	}

	public ITextEditor getEditor() {
		return editor;
	}

	@Override
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset) {
		return new TypeScriptContentAssistInvocationContext(viewer, offset, fEditor);
	}

}
