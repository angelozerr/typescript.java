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

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getKindModifiers() {
		return kindModifiers;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setSpans(List<TextSpan> spans) {
		this.spans = spans;
	}

	public List<TextSpan> getSpans() {
		return spans;
	}

	public boolean hasSpans() {
		return spans != null && spans.size() > 0;
	}

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
