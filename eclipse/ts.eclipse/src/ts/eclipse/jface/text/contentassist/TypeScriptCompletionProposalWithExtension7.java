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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.viewers.StyledString;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * {@link ICompletionProposal} implementation with TypeScript completion entry
 * by implementing Neon {@link ICompletionProposalExtension7}.
 */
public class TypeScriptCompletionProposalWithExtension7 extends TypeScriptCompletionProposal
		implements ICompletionProposalExtension7 {

	public TypeScriptCompletionProposalWithExtension7(String name, String kind, String kindModifiers, String sortText,
			int position, String prefix, String fileName, int line, int offset, ICompletionEntryMatcher matcher,
			ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, position, prefix, fileName, line, offset, matcher, client);
	}

	@Override
	public StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider) {
		// Highlight matched prefix
		StyledString styledDisplayString = new StyledString();
		styledDisplayString.append(getStyledDisplayString());

		String pattern = getPatternToEmphasizeMatch(document, offset);
		if (pattern != null && pattern.length() > 0) {
			String displayString = styledDisplayString.getString();
			int[] bestSequence = getMatcher().bestSubsequence(displayString, pattern);
			int highlightAdjustment = 0;
			for (int index : bestSequence) {
				styledDisplayString.setStyle(index + highlightAdjustment, 1, boldStylerProvider.getBoldStyler());
			}
		}
		return styledDisplayString;
	}
}
