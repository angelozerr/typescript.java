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
package ts;

/**
 * TypeScript model kind.
 *
 */
public enum TypeScriptKind {

	ALIAS, PRIMITIVE_TYPE, KEYWORD, CLASS, INTERFACE, MODULE, SCRIPT, DIRECTORY, PROPERTY, METHOD, CONSTRUCTOR, FUNCTION, VAR, LET, ENUM, PRIVATE, PUBLIC, STATIC, TYPE, ELEMENT, ATTRIBUTE, COMPONENT;

	public static TypeScriptKind getKind(String kind) {
		try {
			return TypeScriptKind.valueOf(kind.toUpperCase());
		} catch (Throwable e) {
			return null;
		}
	}
}
