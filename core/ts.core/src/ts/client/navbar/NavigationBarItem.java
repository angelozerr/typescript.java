package ts.client.navbar;

import java.util.List;

public class NavigationBarItem implements INavigationBarItem {

	private String text;
	private String kind;
	private String kindModifiers;
	private List<TextSpan> spans;
	private List<NavigationBarItem> childItems;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	// public void setSpansA(List<TextSpan> spans) {
	// this.spans = spans;
	// }
	//
	// public List<TextSpan> getSpansA() {
	// return spans;
	// }

	public List<NavigationBarItem> getChildItems() {
		return childItems;
	}

	public void setChildItems(List<NavigationBarItem> childItems) {
		this.childItems = childItems;
	}

	public boolean hasChildItems() {
		return childItems != null && childItems.size() > 0;
	}

}
