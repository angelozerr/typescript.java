package ts.internal.client.protocol;

import java.util.List;

import ts.client.navbar.NavigationBarItem;

public class NavBarResponse {

	private List<NavigationBarItem> body;

	public List<NavigationBarItem> getBody() {
		return body;
	}

	public void setBody(List<NavigationBarItem> body) {
		this.body = body;
	}
}
