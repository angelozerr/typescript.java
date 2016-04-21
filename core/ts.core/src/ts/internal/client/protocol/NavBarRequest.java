/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.internal.client.protocol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ts.TypeScriptException;
import ts.client.navbar.ITypeScriptNavBarCollector;
import ts.client.navbar.NavigationBarItem;

/**
 * NavBar items request; value of command field is "navbar". Return response
 * giving the list of navigation bar entries extracted from the requested file.
 */
public class NavBarRequest extends FileRequest<ITypeScriptNavBarCollector> {

	public NavBarRequest(String fileName, ITypeScriptNavBarCollector collector) {
		super(CommandNames.NavBar.getName(), new FileRequestArgs(fileName), null);
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		// None response

		//System.err.println(response);

//		Type myType = new TypeToken<List<NavigationBarItem>>() {
//		}.getType();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		NavBarResponse a = gson.fromJson(response.toString(), NavBarResponse.class);
		getCollector().setNavBar(a.getBody());
//
//		JsonArray array = response.get("body").asArray();
//
//		List<NavigationBarItem> items = createChildItems(array);
//		getCollector().setNavBar(items);
//		System.err.println(items);
	}

	private List<NavigationBarItem> createChildItems(JsonArray array) {
		List<NavigationBarItem> items = new ArrayList<NavigationBarItem>();
				
		for (JsonValue value : array) {
			JsonObject o = (JsonObject) value;
		
			NavigationBarItem item = new NavigationBarItem();
			item.setText(o.getString("text", ""));
			items.add(item);
			
			JsonValue childItems = o.get("childItems");
			if (childItems != null && childItems.isArray()) {
				//item.setChildItems(createChildItems(childItems.asArray()));
			}
		}
		return items;
	}
}
