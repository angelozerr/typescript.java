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

package ts.eclipse.jface.text.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.AbstractCompletionCollector;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * TypeScript completion collector to build a list of
 * {@link ICompletionProposal}.
 */
public class CompletionProposalCollector extends AbstractCompletionCollector {

	private final List<ICompletionProposal> proposals;
	private final int position;

	public CompletionProposalCollector(int position, String prefix, ICompletionEntryMatcher matcher) {
		super(prefix, matcher);
		this.position = position;
		this.proposals = new ArrayList<ICompletionProposal>();
	}

	@Override
	protected void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		String prefix = getPrefix();
		ICompletionProposal proposal = new TypeScriptCompletionProposal(name, kind, kindModifiers, sortText, position,
				prefix, fileName, line, offset, client, getMatcher());
		proposals.add(proposal);
	}

	public List<ICompletionProposal> getProposals() {
		return proposals;
	}
}
