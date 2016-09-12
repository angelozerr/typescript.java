/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.jsdt.ui.template.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.ui.editor.contentassist.TypeScriptContentAssistInvocationContext;
import ts.eclipse.ide.jsdt.ui.template.ITemplateImageProvider;
import ts.eclipse.ide.jsdt.ui.template.TemplateEngine;

/**
 * Abstract class for template completion proposal computer.
 *
 */
public abstract class AbstractTemplateCompletionProposalComputer implements IJavaCompletionProposalComputer {

	/**
	 * The engine for the current session, if any
	 */
	private TemplateEngine fEngine;

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (!(context instanceof TypeScriptContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		TypeScriptContentAssistInvocationContext javaContext = (TypeScriptContentAssistInvocationContext) context;

		fEngine = computeCompletionEngine(javaContext);
		if (fEngine == null)
			return Collections.emptyList();

		fEngine.reset();
		fEngine.complete(context.getViewer(), context.getInvocationOffset());

		TemplateProposal[] templateProposals = fEngine.getResults();
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(Arrays.asList(templateProposals));

		/*
		 * IJavaCompletionProposal[] keyWordResults =
		 * javaContext.getKeywordProposals(); if (keyWordResults.length > 0) {
		 * List<TemplateProposal> removals = new ArrayList<TemplateProposal>();
		 * 
		 * // update relevance of template proposals that match with a keyword
		 * // give those templates slightly more relevance than the keyword to
		 * // sort them first // remove keyword templates that don't have an
		 * equivalent // keyword proposal if (keyWordResults.length > 0) {
		 * outer: for (int k = 0; k < templateProposals.length; k++) {
		 * TemplateProposal curr = templateProposals[k]; String name =
		 * curr.getTemplate().getName();
		 * 
		 * for (int i = 0; i < keyWordResults.length; i++) { String keyword =
		 * keyWordResults[i].getDisplayString(); if (name.startsWith(keyword)) {
		 * curr.setRelevance(keyWordResults[i].getRelevance() + 1); continue
		 * outer; } }
		 * 
		 * if (isKeyword(name)) { removals.add(curr); } } }
		 * 
		 * result.removeAll(removals); }
		 */
		return result;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionStarted() {

	}

	@Override
	public void sessionEnded() {
		if (fEngine != null) {
			fEngine.reset();
			fEngine = null;
		}
	}

	protected static TemplateEngine createTemplateEngine(String contextTypeId, ITemplateImageProvider imageProvider) {
		return createTemplateEngine(JSDTTypeScriptUIPlugin.getDefault().getTemplateContextRegistry(), contextTypeId,
				imageProvider);
	}

	protected static TemplateEngine createTemplateEngine(ContextTypeRegistry templateContextRegistry,
			String contextTypeId, ITemplateImageProvider imageProvider) {
		TemplateContextType contextType = templateContextRegistry.getContextType(contextTypeId);
		Assert.isNotNull(contextType);
		return new TemplateEngine(contextType, imageProvider);
	}

	/**
	 * Compute the engine used to retrieve completion proposals in the given
	 * context
	 *
	 * @param context
	 *            the context where proposals will be made
	 * @return the engine or <code>null</code> if no engine available in the
	 *         context
	 */
	protected abstract TemplateEngine computeCompletionEngine(TypeScriptContentAssistInvocationContext context);

}
