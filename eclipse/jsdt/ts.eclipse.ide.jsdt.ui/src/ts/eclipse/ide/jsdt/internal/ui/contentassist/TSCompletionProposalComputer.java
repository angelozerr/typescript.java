/**
 *  Copyright (c) 2013-2015 Angelo ZERR and Genuitec LLC.
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
package ts.eclipse.ide.jsdt.internal.ui.contentassist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;

import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.server.ITSClient;
import ts.server.nodejs.NodeJSTSClient;

/**
 * JSDT completion proposal computer manage completion Proposal for Javascript
 * (IJavaCompletionProposalComputer - inside JavaScript files) and for HTML
 * (ICompletionProposalComputer - inside HTML files).
 * 
 */
public class TSCompletionProposalComputer
		implements IJavaCompletionProposalComputer/* , ICompletionProposalComputer */ {

	private static ITSClient client;

	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
			IProject project = javaContext.getProject().getProject();
			try {
				if (client == null) {
					File projectDir = project.getLocation().toFile();
					File tsRepositoryFile = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
					File tsserverFile = new File(tsRepositoryFile, "node_modules/typescript/bin/tsserver");
					client = new NodeJSTSClient(projectDir, tsserverFile, null);
				}
				IDocument document = javaContext.getDocument();
				IResource resource = javaContext.getCompilationUnit()
						.getResource();		
				
				String fileName = resource.getName();
				client.openFile(fileName);
				client.updateFile(fileName, document.get());
				
				int position = javaContext.getInvocationOffset();
				int line = document.getLineOfOffset(position);
				int offset = position - document.getLineOffset(line);
				String prefix = "";
				
				List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
				ICompletionInfo completion = client.getCompletionsAtLineOffset(fileName, line, offset, prefix);
				for (ICompletionEntry entry: completion.getEntries()) {
					proposals.add(new CompletionProposal(entry.getName(), 0, 0, 0));
				}
				return proposals;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
		// return computeCompletionProposals(context);
	}

	// @Override
	// public List computeCompletionProposals(
	// CompletionProposalInvocationContext context, IProgressMonitor monitor) {
	// //return computeCompletionProposals(context);
	// }

	/*
	 * private List<?> computeCompletionProposals(Object context) {
	 * 
	 * 
	 * TernContext tsContext =
	 * JSDTTernUIPlugin.getContextProvider().getTernContext(context); if
	 * (tsContext != null) { try {
	 * 
	 * final List<ICompletionProposal> proposals = new
	 * ArrayList<ICompletionProposal>();
	 * 
	 * ITernFile tf = tsContext.file; ITernProject tsProject =
	 * tsContext.project; IProject project =
	 * (IProject)tsProject.getAdapter(IProject.class);
	 * 
	 * int startOffset = tsContext.invocationOffset; String filename =
	 * tf.getFullName(tsProject); TernCompletionsQuery query =
	 * TernCompletionsQueryFactory .createQuery(project, filename, startOffset);
	 * 
	 * tsProject.request(query, tf, new JSDTTernCompletionCollector(proposals,
	 * startOffset, tf, tsProject)); return proposals;
	 * 
	 * } catch (Exception e) { Trace.trace(Trace.SEVERE,
	 * "Error while JSDT Tern completion.", e); } } return
	 * Collections.EMPTY_LIST; }
	 */

	/*
	 * @Override public List computeContextInformation(
	 * CompletionProposalInvocationContext context, IProgressMonitor moniotr) {
	 * return Collections.EMPTY_LIST; }
	 */

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
