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
package ts.resources;

import ts.TypeScriptException;
import ts.client.Location;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;

/**
 * TypeScript file API.
 *
 */
public interface ITypeScriptFile {

	ITypeScriptProject getProject();

	String getName();

	boolean isOpened();

	boolean isDirty();

	void setDirty(boolean dirty);

	String getPrefix(int position);

	Location getLocation(int position) throws TypeScriptException;

	int getPosition(int line, int offset) throws TypeScriptException;

	String getContents();

	void open() throws TypeScriptException;

	void close() throws TypeScriptException;

	void synch() throws TypeScriptException;

	void completions(int position, ITypeScriptCompletionCollector collector) throws TypeScriptException;

	void definition(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException;

}
