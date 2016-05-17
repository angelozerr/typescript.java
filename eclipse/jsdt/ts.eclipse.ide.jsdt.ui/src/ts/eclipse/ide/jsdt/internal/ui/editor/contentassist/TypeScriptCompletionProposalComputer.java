/**
 *  Copyright (c) 2015-2016 Angelo ZERR and Genuitec LLC.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Piotr Tomiak <piotr@genuitec.com> - refactoring of file management API
 *  									unified completion proposals calculation
 */
package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.Trace;
import ts.eclipse.jface.text.contentassist.CompletionProposalCollector;
import ts.resources.ITypeScriptFile;

/**
 * JSDT completion proposal computer manage completion Proposal for Javascript
 * (IJavaCompletionProposalComputer - inside JavaScript files) and for HTML
 * (ICompletionProposalComputer - inside HTML files).
 * 
 */
public class TypeScriptCompletionProposalComputer
		implements IJavaCompletionProposalComputer/* , ICompletionProposalComputer */ {

	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		IResource resource = null;
		if (context instanceof TypeScriptContentAssistInvocationContext) {
			TypeScriptContentAssistInvocationContext tsContext = (TypeScriptContentAssistInvocationContext) context;
			resource = tsContext.getResource();
		} else if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
			resource = javaContext.getCompilationUnit().getResource();
		}
		if (resource != null) {
			try {
				if (TypeScriptResourceUtil.canConsumeTsserver(resource)) {
					IProject project = resource.getProject();
					IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
					if (tsProject != null) {

						int position = context.getInvocationOffset();

						IDocument document = context.getDocument();
						ITypeScriptFile tsFile = tsProject.openFile(resource, document);
						CharSequence prefix = context.computeIdentifierPrefix();

						CompletionProposalCollector collector = new CompletionProposalCollector(position,
								prefix != null ? prefix.toString() : null,
								tsProject.getProjectSettings().getCompletionEntryMatcher());
						tsFile.completions(position, collector);
						return collector.getProposals();
					}
				}
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error while TypeScript completion", e);
			}
		}
		return Collections.EMPTY_LIST;
	}

	public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return Collections.EMPTY_LIST;
	}

	public String getErrorMessage() {
		return null;
	}

	public void sessionStarted() {
	}

	public void sessionEnded() {
	}
}
