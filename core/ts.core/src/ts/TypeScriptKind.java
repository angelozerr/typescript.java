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
 * TypeScript mode kind.
 *
 */
public enum TypeScriptKind {

	/** Primitive type. */
	PRIMITIVE_TYPE, /** Keyword. */
	KEYWORD, /** Class. */
	CLASS, /** Interface. */
	INTERFACE, /** Module. */
	MODULE, /** Property. */
	PROPERTY, /** Method. */
	METHOD, /** Constructor. */
	CONSTRUCTOR, /** Function. */
	FUNCTION, /** Variable. */
	VAR, /** Enumeration. */
	ENUM, /** Private modifier. */
	PRIVATE, /** Public modifier. */
	PUBLIC, /** Static modifier. */
	STATIC, /** Type. */
	TYPE;

	public static TypeScriptKind getKind(String kind) {
		try {
			return TypeScriptKind.valueOf(kind.toUpperCase());
		} catch (Throwable e) {
			return null;
		}
	}
}
