package ts.eclipse.ide.jsdt.internal.ui.template.contentassist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.core.template.ReactContextType;
import ts.eclipse.ide.jsdt.internal.ui.text.jsx.IJSXPartitions;
import ts.eclipse.ide.jsdt.ui.editor.contentassist.TypeScriptContentAssistInvocationContext;
import ts.eclipse.ide.jsdt.ui.template.ITemplateImageProvider;
import ts.eclipse.ide.jsdt.ui.template.TemplateEngine;
import ts.eclipse.ide.jsdt.ui.template.contentassist.AbstractTemplateCompletionProposalComputer;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class ReactTemplateCompletionProposalComputer extends AbstractTemplateCompletionProposalComputer
		implements ITemplateImageProvider {

	private final TemplateEngine reactTemplateEngine;

	public ReactTemplateCompletionProposalComputer() {
		reactTemplateEngine = createTemplateEngine(ReactContextType.NAME, this);
	}

	@Override
	protected TemplateEngine computeCompletionEngine(TypeScriptContentAssistInvocationContext context) {
		try {
			if (!TypeScriptResourceUtil.isTsxOrJsxFile(context.getResource())) {
				return null;
			}
			String partition = TextUtilities.getContentType(context.getDocument(),
					IJavaScriptPartitions.JAVA_PARTITIONING, context.getInvocationOffset(), true);
			if (partition.equals(IJavaScriptPartitions.JAVA_DOC)) {
				return null;
			} else if (partition.equals(IJSXPartitions.JSX)) {
				return null;
			} else {
				return reactTemplateEngine;
			}
		} catch (BadLocationException x) {
			return null;
		}
	}

	@Override
	public Image getImage(Template template) {
		return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_JSX);
	}
}
