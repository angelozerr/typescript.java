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

import ts.client.AbstractTypeScriptCollector;
import ts.client.ITypeScriptServiceClient;

public abstract class AbstractCompletionCollector extends AbstractTypeScriptCollector
		implements ITypeScriptCompletionCollector {

	private final ICompletionEntryMatcher matcher;
	private final String prefix;

	public AbstractCompletionCollector(String prefix, ICompletionEntryMatcher matcher) {
		this.matcher = matcher != null ? matcher : ICompletionEntryMatcher.START_WITH_MATCHER;
		this.prefix = prefix != null ? prefix : "";
	}

	@Override
	public void addCompletionEntry(String name, String kind, String kindModifiers, String sortText, String fileName,
			int line, int offset, ITypeScriptServiceClient client) {
		if (matcher.match(name, prefix)) {
			doAddCompletionEntry(name, kind, kindModifiers, sortText, fileName, line, offset, client);
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public ICompletionEntryMatcher getMatcher() {
		return matcher;
	}

	protected abstract void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client);
}
