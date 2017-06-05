package org.eclipse.jface.text.provisional.codelens.internal;

import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.ICodeLensProvider;

public class CodeLensData {

	private final ICodeLens symbol;
	private final ICodeLensProvider provider;

	public CodeLensData(ICodeLens symbol, ICodeLensProvider provider) {
		this.symbol = symbol;
		this.provider = provider;
	}

	public ICodeLens getSymbol() {
		return symbol;
	}

	public ICodeLensProvider getProvider() {
		return provider;
	}
}
