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

public class CompletionEntry implements ICompletionEntry, ITypeScriptCompletionEntryDetailsCollector {

	private final String name;
	private final String kind;
	private final String kindModifiers;
	private final String sortText;

	private final String fileName;
	private final int line;
	private final int offset;
	private final ITypeScriptServiceClient client;
	private CompletionEntryDetails entryDetails;
	private ICompletionEntryMatcher matcher;

	public CompletionEntry(String name, String kind, String kindModifiers, String sortText, String fileName, int line,
			int offset, ITypeScriptServiceClient client, ICompletionEntryMatcher matcher) {
		this.name = name;
		this.kind = kind;
		this.kindModifiers = kindModifiers;
		this.sortText = sortText;
		this.fileName = fileName;
		this.line = line;
		this.offset = offset;
		this.client = client;
		this.matcher = matcher;
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
}
