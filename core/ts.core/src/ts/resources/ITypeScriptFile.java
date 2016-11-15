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
import ts.client.IPositionProvider;
import ts.client.codefixes.ITypeScriptGetCodeFixesCollector;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.client.format.FormatOptions;
import ts.client.format.ITypeScriptFormatCollector;
import ts.client.navbar.ITypeScriptNavBarCollector;
import ts.client.navbar.NavigationBarItemRoot;
import ts.client.occurrences.ITypeScriptOccurrencesCollector;
import ts.client.references.ITypeScriptReferencesCollector;

/**
 * TypeScript file API.
 *
 */
public interface ITypeScriptFile extends IPositionProvider {

	/**
	 * Returns the owner project of the file.
	 * 
	 * @return the owner project of the file.
	 */
	ITypeScriptProject getProject();

	/**
	 * Returns the full path of the file.
	 * 
	 * @return the full path of the file.
	 */
	String getName();

	/**
	 * Returns true if the file is flagged as "open" in tsserver side and false
	 * otherwise. In the case where tsserver is not started and the file is
	 * opened in the IDE editor, this method returns false.
	 * 
	 * @return true if the file is flagged as "open" in tsserver side and false
	 *         otherwise.
	 */
	boolean isOpened();

	/**
	 * Returns true if file content has changed and must be synchronized with
	 * tsserver and false otherwise.
	 * 
	 * @return true if file content has changed and must be synchronized with
	 *         tsserver and false otherwise.
	 */
	boolean isDirty();

	String getPrefix(int position);

	String getContents();

	/**
	 * Flag the file as "opened" into tsserver side.
	 * 
	 * @throws TypeScriptException
	 */
	void open() throws TypeScriptException;

	/**
	 * Flag the file as "closed" into tsserver side.
	 * 
	 * @throws TypeScriptException
	 */
	void close() throws TypeScriptException;

	/**
	 * Synchronize file content with tsserver according the
	 * {@link SynchStrategy}.
	 * 
	 * @throws TypeScriptException
	 */
	void synch() throws TypeScriptException;

	/**
	 * Call completions from the tsserver.
	 * 
	 * @param position
	 * @param collector
	 * @throws TypeScriptException
	 */
	void completions(int position, ITypeScriptCompletionCollector collector) throws TypeScriptException;

	/**
	 * Call definition from the tsserver.
	 * 
	 * @param position
	 * @param collector
	 * @throws TypeScriptException
	 */
	void definition(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException;

	/**
	 * Format the file content according start/end position.
	 * 
	 * @param startPosition
	 * @param endPosition
	 * @param collector
	 * @throws TypeScriptException
	 */
	void format(int startPosition, int endPosition, ITypeScriptFormatCollector collector) throws TypeScriptException;

	/**
	 * Find references of the given position.
	 * 
	 * @param position
	 * @param collector
	 * @throws TypeScriptException
	 */
	void references(int position, ITypeScriptReferencesCollector collector) throws TypeScriptException;

	/**
	 * Find occurrences of the given position.
	 * 
	 * @param position
	 * @param collector
	 * @throws TypeScriptException
	 */
	void occurrences(int position, ITypeScriptOccurrencesCollector collector) throws TypeScriptException;

	/**
	 * Nav bar for the file.
	 * 
	 * @param collector
	 * @throws TypeScriptException
	 */
	void navbar(ITypeScriptNavBarCollector collector) throws TypeScriptException;

	/**
	 * Call implementation from the tsserver.
	 * 
	 * @param position
	 * @param collector
	 * @throws TypeScriptException
	 */
	void implementation(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException;

	/**
	 * Get code fixes.
	 * 
	 * @param startPosition
	 * @param endPosition
	 * @param collector
	 * @throws TypeScriptException
	 */
	void getCodeFixes(int startPosition, int endPosition, String[] errorCodes,
			ITypeScriptGetCodeFixesCollector collector) throws TypeScriptException;

	void addNavbarListener(INavbarListener listener);

	void removeNavbarListener(INavbarListener listener);

	NavigationBarItemRoot getNavBar();

	FormatOptions getFormatOptions();

	void setFormatOptions(FormatOptions formatOptions);
}
