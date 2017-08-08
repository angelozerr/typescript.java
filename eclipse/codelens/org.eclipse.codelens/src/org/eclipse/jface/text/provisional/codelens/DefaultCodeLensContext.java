package org.eclipse.jface.text.provisional.codelens;

import org.eclipse.jface.text.ITextViewer;

public class DefaultCodeLensContext implements ICodeLensContext {

	private final ITextViewer textViewer;

	public DefaultCodeLensContext(ITextViewer textViewer) {
		this.textViewer = textViewer;
	}

	@Override
	public ITextViewer getViewer() {
		return textViewer;
	}

}
