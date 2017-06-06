package org.eclipse.jface.text.provisional.codelens.internal;

import org.eclipse.jface.text.provisional.viewzones.ViewZone;

public class CodeLensViewZone extends ViewZone {

	private String text;

	public CodeLensViewZone(int afterLineNumber, int height) {
		super(afterLineNumber, height, CodeLensViewZoneRenderer.getInstance());
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}