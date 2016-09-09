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

import com.eclipsesource.json.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import ts.TypeScriptException;
import ts.client.IPositionProvider;
import ts.client.Location;
import ts.client.navbar.ITypeScriptNavBarCollector;
import ts.client.navbar.NavigationBarItemRoot;

/**
 * NavBar items request; value of command field is "navbar". Return response
 * giving the list of navigation bar entries extracted from the requested file.
 */
public class NavBarRequest extends FileRequest<ITypeScriptNavBarCollector> {

	private final IPositionProvider positionProvider;

	public NavBarRequest(String fileName, IPositionProvider positionProvider, ITypeScriptNavBarCollector collector) {
		super(CommandNames.NavBar.getName(), new FileRequestArgs(fileName), null);
		super.setCollector(collector);
		this.positionProvider = positionProvider;
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		// None response

		// System.err.println(response);

		// Type myType = new TypeToken<List<NavigationBarItem>>() {
		// }.getType();

		// Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new InstanceCreator<Location>() {
			@Override
			public Location createInstance(Type type) {
				return new Location(positionProvider);
			}
		}).create();

		NavBarResponse a = gson.fromJson(response.toString(), NavBarResponse.class);
		getCollector().setNavBar(new NavigationBarItemRoot(a.getBody()));
	}

}
