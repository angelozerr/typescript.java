package ts.client.navbar;

import java.util.List;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptNavBarCollector extends ITypeScriptCollector {

	void setNavBar(List<NavigationBarItem> list);

}
