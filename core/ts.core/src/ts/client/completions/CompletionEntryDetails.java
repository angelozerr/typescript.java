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

public class CompletionEntryDetails implements ICompletionEntryDetails {

	private final String name;
	private final String kind;
	private final String kindModifiers;
	private final List<SymbolDisplayPart> displayParts;
	private final List<SymbolDisplayPart> documentation;

	public CompletionEntryDetails(String name, String kind, String kindModifiers) {
		this.name = name;
		this.kind = kind;
		this.kindModifiers = kindModifiers;
		this.displayParts = new ArrayList<SymbolDisplayPart>();
		this.documentation = new ArrayList<SymbolDisplayPart>();
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

	public void addDisplayPart(String text, String kind) {
		this.displayParts.add(new SymbolDisplayPart(text, kind));
	}

	@Override
	public List<SymbolDisplayPart> getDisplayParts() {
		return displayParts;
	}

	public void addDocumentation(String text, String kind2) {
		this.documentation.add(new SymbolDisplayPart(text, kind));
	}

	@Override
	public List<SymbolDisplayPart> getDocumentation() {
		return documentation;
	}

}
