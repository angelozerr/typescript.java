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
package ts.eclipse.ide.server.nodejs.internal.ui.console;

import ts.client.ITypeScriptServiceClient;
import ts.client.TypeScriptServiceClient;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;

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

	private TypeScriptNodejsInterceptor getInterceptor(IIDETypeScriptProject project) {
		String key = TypeScriptNodejsInterceptor.class.getName();
		TypeScriptNodejsInterceptor interceptor = project.getData(key);
		if (interceptor == null) {
			interceptor = new TypeScriptNodejsInterceptor(project);
			project.setData(key, interceptor);
		}
		return interceptor;
	}

	@Override
	public void connectToInstallTypesConsole(ITypeScriptServiceClient client) {
		client.addInstallTypesListener(InstallTypesConsoleListener.INSTANCE);
	}

	@Override
	public void disconnectToInstallTypesConsole(ITypeScriptServiceClient client) {
		client.removeInstallTypesListener(InstallTypesConsoleListener.INSTANCE);
	}
}
