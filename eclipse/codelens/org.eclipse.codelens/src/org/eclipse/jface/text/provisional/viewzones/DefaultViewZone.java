package org.eclipse.jface.text.provisional.viewzones;

public class DefaultViewZone extends ViewZone {

	private String text;

	public DefaultViewZone(int afterLineNumber, int height) {
		super(afterLineNumber, height, DefaultViewZoneRenderer.getInstance());
	}

	public DefaultViewZone(int afterLineNumber, int height, String text) {
		this(afterLineNumber, height);
		setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
