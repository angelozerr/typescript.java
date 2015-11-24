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
package ts.server;

/**
 * This adapter class provides default implementations for the methods described
 * by the {@link ITypeScriptServerListener} interface.
 * 
 * Classes that wish to deal with event can extend this class and override only
 * the methods which they are interested in.
 * 
 */
public class TypeScriptServerAdapter implements ITypeScriptServerListener {

	@Override
	public void onStart(ITypeScriptServiceClient server) {

	}

	@Override
	public void onStop(ITypeScriptServiceClient server) {

	}

}
