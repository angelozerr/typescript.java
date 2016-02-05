/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.server.nodejs.internal.ui.console;

import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.server.ITypeScriptServiceClient;
import ts.server.TypeScriptServiceClient;

public class TypeScriptConsoleNodejsConnector implements ITypeScriptConsoleConnector {

	@Override
	public boolean isAdaptFor(ITypeScriptServiceClient client) {
		return client instanceof TypeScriptServiceClient;
	}

	@Override
	public void connectToConsole(ITypeScriptServiceClient client, IIDETypeScriptProject project) {
		TypeScriptServiceClient nodeServer = (TypeScriptServiceClient) client;
		TypeScriptNodejsInterceptor interceptor = getInterceptor(project);
		nodeServer.addInterceptor(interceptor);
		nodeServer.addProcessListener(interceptor);
	}

	@Override
	public void disconnectToConsole(ITypeScriptServiceClient client, IIDETypeScriptProject project) {
		TypeScriptServiceClient nodeServer = (TypeScriptServiceClient) client;
		TypeScriptNodejsInterceptor interceptor = getInterceptor(project);
		nodeServer.removeInterceptor(interceptor);
		nodeServer.removeProcessListener(interceptor);
	}

	public TypeScriptNodejsInterceptor getInterceptor(IIDETypeScriptProject project) {
		String key = TypeScriptNodejsInterceptor.class.getName();
		TypeScriptNodejsInterceptor interceptor = project.getData(key);
		if (interceptor == null) {
			interceptor = new TypeScriptNodejsInterceptor(project);
			project.setData(key, interceptor);
		}
		return interceptor;
	}
}
