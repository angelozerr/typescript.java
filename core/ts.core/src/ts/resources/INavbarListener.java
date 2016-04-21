package ts.resources;

import java.util.List;

import ts.client.navbar.NavigationBarItem;

public interface INavbarListener {

	void navBarChanged(List<NavigationBarItem> items);
}
