package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavadocCompletionProcessor;

public class TypeScriptJavadocCompletionProcessor extends JavaCompletionProcessor {

	private int fSubProcessorFlags;

	public TypeScriptJavadocCompletionProcessor(IEditorPart editor, ContentAssistant assistant) {
		super(editor, assistant, IJavaScriptPartitions.JAVA_DOC);
		fSubProcessorFlags = 0;
	}

	/**
	 * Tells this processor to restrict is proposals to those starting with
	 * matching cases.
	 *
	 * @param restrict
	 *            <code>true</code> if proposals should be restricted
	 */
	public void restrictProposalsToMatchingCases(boolean restrict) {
		fSubProcessorFlags = restrict ? IJavadocCompletionProcessor.RESTRICT_TO_MATCHING_CASE : 0;
	}

	@Override
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset) {
		return new TypeScriptJavadocContentAssistInvocationContext(viewer, offset, fEditor, fSubProcessorFlags);
	}

}
