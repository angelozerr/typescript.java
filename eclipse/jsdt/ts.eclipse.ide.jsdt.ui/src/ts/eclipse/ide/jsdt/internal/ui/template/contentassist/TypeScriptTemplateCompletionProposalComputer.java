package ts.eclipse.ide.jsdt.internal.ui.template.contentassist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;

import ts.eclipse.ide.jsdt.core.template.JSDocContextType;
import ts.eclipse.ide.jsdt.core.template.TypeScriptContextType;
import ts.eclipse.ide.jsdt.ui.editor.contentassist.TypeScriptContentAssistInvocationContext;
import ts.eclipse.ide.jsdt.ui.template.TemplateEngine;
import ts.eclipse.ide.jsdt.ui.template.contentassist.AbstractTemplateCompletionProposalComputer;

public class TypeScriptTemplateCompletionProposalComputer extends AbstractTemplateCompletionProposalComputer {

	private final TemplateEngine typeScriptTemplateEngine;
	private final TemplateEngine jsDocTemplateEngine;

	public TypeScriptTemplateCompletionProposalComputer() {
		typeScriptTemplateEngine = createTemplateEngine(TypeScriptContextType.NAME, null);
		jsDocTemplateEngine = createTemplateEngine(JSDocContextType.NAME, null);
	}

	@Override
	protected TemplateEngine computeCompletionEngine(TypeScriptContentAssistInvocationContext context) {
		try {
			String partition = TextUtilities.getContentType(context.getDocument(),
					IJavaScriptPartitions.JAVA_PARTITIONING, context.getInvocationOffset(), true);
			if (partition.equals(IJavaScriptPartitions.JAVA_DOC)) {
				return jsDocTemplateEngine;
			} else {
				return typeScriptTemplateEngine;
			}
		} catch (BadLocationException x) {
			return null;
		}
	}
}
