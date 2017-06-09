/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.swt.custom.patch.internal;

import java.lang.reflect.Method;

import org.eclipse.swt.graphics.TextLayout;

import javassist.util.proxy.MethodHandler;

/**
 * Javassist method handler to override getTextLayout of StyledTextRenderer.
 *
 */
public class StyledTextRendererEmulator implements MethodHandler {

	@Override
	public Object invoke(Object obj, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		if ("getTextLayout".equals(thisMethod.getName()) && args.length > 1) {
			int lineIndex = (int) args[0];
			int orientation = (int) args[1];
			int width = (int) args[2];
			int lineSpacing = (int) args[3];
			return getTextLayout(lineIndex, orientation, width, lineSpacing, obj, proceed, args);
		}
		return proceed.invoke(obj, args);
	}

	protected TextLayout getTextLayout(int lineIndex, int orientation, int width, int lineSpacing, Object obj,
			Method proceed, Object[] args) throws Exception {
		args[0] = lineIndex;
		args[1] = orientation;
		args[2] = width;
		args[3] = lineSpacing;
		return (TextLayout) proceed.invoke(obj, args);
	}

}