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

import java.util.ArrayList;
import java.util.List;

import ts.client.ITypeScriptServiceClient;

public class CompletionInfo extends AbstractCompletionCollector implements ICompletionInfo {

	private final List<ICompletionEntry> entries;

	public CompletionInfo(String prefix) {
		this(prefix, null);
	}

	public CompletionInfo(String prefix, ICompletionEntryMatcher matcher) {
		super(prefix, matcher);
		this.entries = new ArrayList<ICompletionEntry>();
	}

	@Override
	protected ICompletionEntry createEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		return new CompletionEntry(name, kind, kindModifiers, sortText, fileName, line, offset, getMatcher(), client);
	}

	@Override
	protected void addCompletionEntry(ICompletionEntry entry) {
		entries.add(entry);
	}

	@Override
	public boolean isMemberCompletion() {
		return false;
	}

	@Override
	public boolean isNewIdentifierLocation() {
		return false;
	}

	@Override
	public ICompletionEntry[] getEntries() {
		return entries.toArray(ICompletionEntry.EMPTY_ENTRIES);
	}

}
