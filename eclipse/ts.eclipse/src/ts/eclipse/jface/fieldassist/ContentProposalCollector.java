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
package ts.eclipse.jface.fieldassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.AbstractCompletionCollector;
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * TypeScript completion collector to build a list of {@link IContentProposal}.
 */
public class ContentProposalCollector extends AbstractCompletionCollector {

	public static final IContentProposal[] EMPTY_PROPOSAL = new IContentProposal[0];
	private final List<IContentProposal> proposals;

	public ContentProposalCollector(String prefix, ICompletionEntryMatcher matcher) {
		super(prefix, matcher);
		this.proposals = new ArrayList<IContentProposal>();
	}

	@Override
	protected ICompletionEntry createEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		return new TypeScriptContentProposal(name, kind, kindModifiers, sortText, getPrefix(), fileName, line, offset,
				getMatcher(), client);
	}

	@Override
	protected void addCompletionEntry(ICompletionEntry entry) {
		proposals.add((IContentProposal) entry);
	}

	public List<IContentProposal> getProposals() {
		return proposals;
	}
}
