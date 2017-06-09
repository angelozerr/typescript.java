package org.eclipse.jface.text.provisional.codelens.internal;

import org.eclipse.jface.text.provisional.viewzones.ViewZone;
import org.eclipse.swt.events.MouseEvent;

public class CodeLensViewZone extends ViewZone {

	private String text;
	private MouseEvent hover;

	public CodeLensViewZone(int afterLineNumber, int height) {
		super(afterLineNumber, height, CodeLensViewZoneRenderer.getInstance());
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void mouseHover(MouseEvent event) {
		hover = event;
	}

	@Override
	public void mouseEnter(MouseEvent event) {
		hover = event;
	}

	@Override
	public void mouseExit(MouseEvent event) {
		hover = null;
	}

	public MouseEvent getHover() {
		return hover;
	}
}