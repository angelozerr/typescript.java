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

import org.eclipse.jface.fieldassist.IContentProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * {@link IContentProposal} implementation with TypeScript completion entry.
 */
public class TypeScriptContentProposal extends CompletionEntry implements IContentProposal {

	private final String prefix;
	private String content;
	private String description;

	public TypeScriptContentProposal(ICompletionEntryMatcher matcher, String fileName, int line, int offset,
			ITypeScriptServiceClient client, String prefix) {
		super(matcher, fileName, line, offset, client);
		this.prefix = prefix;
	}

	@Override
	public String getContent() {
		initIfNeeded();
		return content;
	}

	private void initIfNeeded() {
		if (content == null) {
			String name = super.getName();
			this.content = prefix != null ? name.substring(prefix.length(), name.length()) : name;
			this.description = null;
		}
	}

	@Override
	public int getCursorPosition() {
		initIfNeeded();
		return content.length();
	}

	@Override
	public String getDescription() {
		initIfNeeded();
		return description;
	}

	@Override
	public String getLabel() {
		return getName();
	}

}
