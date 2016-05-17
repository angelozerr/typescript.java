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
package ts.client.completions;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.internal.matcher.LCSS;
import ts.utils.StringUtils;

public class CompletionEntry implements ICompletionEntry, ITypeScriptCompletionEntryDetailsCollector {

	// Negative value ensures subsequence matches have a lower relevance than
	// standard JDT or template proposals
	private static final int SUBWORDS_RANGE_START = -9000;

	private static final int minPrefixLengthForTypes = 1;

	private final String name;
	private final String kind;
	private final String kindModifiers;
	private final String sortText;

	private final String fileName;
	private final int line;
	private final int offset;
	private CompletionEntryDetails entryDetails;
	private final ICompletionEntryMatcher matcher;
	private int relevance;
	private final ITypeScriptServiceClient client;

	public CompletionEntry(String name, String kind, String kindModifiers, String sortText, String fileName, int line,
			int offset, ICompletionEntryMatcher matcher, ITypeScriptServiceClient client) {
		this.name = name;
		this.kind = kind;
		this.kindModifiers = kindModifiers;
		this.sortText = sortText;
		this.fileName = fileName;
		this.line = line;
		this.offset = offset;
		this.matcher = matcher;
		this.client = client;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getKindModifiers() {
		return kindModifiers;
	}

	@Override
	public String getSortText() {
		return sortText;
	}

	public ICompletionEntryDetails getEntryDetails() throws TypeScriptException {
		if (entryDetails != null) {
			return entryDetails;
		}
		client.completionEntryDetails(fileName, line, offset, new String[] { name }, this);
		return this.entryDetails;
	}

	@Override
	public void setEntryDetails(String name, String kind, String kindModifiers) {
		entryDetails = new CompletionEntryDetails(name, kind, kindModifiers);
	}

	@Override
	public void addDisplayPart(String text, String kind) {
		entryDetails.addDisplayPart(text, kind);
	}

	@Override
	public void addDocumentation(String text, String kind) {
		entryDetails.addDocumentation(text, kind);
	}

	public ICompletionEntryMatcher getMatcher() {
		return matcher;
	}

	@Override
	public int getRelevance() {
		return relevance;
	}

	@Override
	public boolean updatePrefix(String prefix) {
		Integer relevanceBoost = null;
		int[] bestSequence = null;
		if (StringUtils.isEmpty(prefix)) {
			relevanceBoost = 0;
		} else {
			bestSequence = matcher.bestSubsequence(name, prefix);
			if ((bestSequence != null && bestSequence.length > 0)) {
				relevanceBoost = 0;
				if (name.equals(prefix)) {
					if (minPrefixLengthForTypes < prefix.length()) {
						relevanceBoost = 16 * (RelevanceConstants.R_EXACT_NAME + RelevanceConstants.R_CASE);
					}
				} else if (name.equalsIgnoreCase(prefix)) {
					if (minPrefixLengthForTypes < prefix.length()) {
						relevanceBoost = 16 * RelevanceConstants.R_EXACT_NAME;
					}
				} else if (startsWithIgnoreCase(prefix, name)) {
					// Don't adjust score
				} else {
					int score = LCSS.scoreSubsequence(bestSequence);
					relevanceBoost = SUBWORDS_RANGE_START + score;
				}

			}
		}
		if (relevanceBoost != null) {
			relevance = relevanceBoost;
			return true;
		}
		return false;
	}

	private boolean startsWithIgnoreCase(String prefix, String name) {
		return prefix.toUpperCase().startsWith(name.toUpperCase());
	}
}
