package ts.eclipse.ide.jsdt.internal.ui.template.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.wst.jsdt.internal.corext.template.java.JavaDocContextType;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import ts.eclipse.ide.jsdt.core.template.TypeScriptContextType;

public class TypeScriptTemplateCompletionProposalComputer implements IJavaCompletionProposalComputer{

	private final TemplateEngine fJavaTemplateEngine;
	private final TemplateEngine fJavadocTemplateEngine;

	public TypeScriptTemplateCompletionProposalComputer() {
		TemplateContextType contextType = JavaScriptPlugin.getDefault().getTemplateContextRegistry().getContextType(TypeScriptContextType.NAME);

		if (contextType == null) {
			contextType = new TypeScriptContextType();
			JavaScriptPlugin.getDefault().getTemplateContextRegistry().addContextType(contextType);
		}

		fJavaTemplateEngine = new TemplateEngine(contextType);
		contextType = JavaScriptPlugin.getDefault().getTemplateContextRegistry().getContextType("javadoc"); //$NON-NLS-1$

		if (contextType == null) {
			contextType = new JavaDocContextType();
			JavaScriptPlugin.getDefault().getTemplateContextRegistry().addContextType(contextType);
		}

		fJavadocTemplateEngine = new TemplateEngine(contextType);
	}
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		TemplateEngine engine;
		try {
			String partition = TextUtilities.getContentType(context.getDocument(), IJavaScriptPartitions.JAVA_PARTITIONING, context.getInvocationOffset(), true);
			if (partition.equals(IJavaScriptPartitions.JAVA_DOC)) {
				engine = fJavadocTemplateEngine;
			} else {
				engine = fJavaTemplateEngine;
			}
		} catch (BadLocationException x) {
			return Collections.emptyList();
		}
		
		if (engine != null) {
			engine.reset();
			engine.complete(context.getViewer(), context.getInvocationOffset());

			TemplateProposal[] templateProposals = engine.getResults();
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(Arrays.asList(templateProposals));

		}
		return Collections.emptyList();
	}
	
	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub
		
	}

}
