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
package ts.eclipse.ide.ui.hyperlink;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import ts.client.TextSpan;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * TypeScript hyperlink.
 *
 */
public class TypeScriptHyperlink implements IHyperlink {

	private final TextSpan span;
	private final IFile file;
	private final File fsFile;
	private final IRegion region;

	/**
	 * TypeScript hyperlink with file coming from the Eclipse workspace.
	 * 
	 * @param file
	 * @param span
	 * @param region
	 */
	public TypeScriptHyperlink(IFile file, TextSpan span, IRegion region) {
		this.file = file;
		this.fsFile = null;
		this.span = span;
		this.region = region;
	}

	/**
	 * TypeScript hyperlink with file coming from the file system.
	 * 
	 * @param fsFile
	 * @param span
	 * @param region
	 */
	public TypeScriptHyperlink(File fsFile, TextSpan span, IRegion region) {
		this.file = null;
		this.fsFile = fsFile;
		this.span = span;
		this.region = region;

	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return TypeScriptUIMessages.TypeScriptHyperlink_typeLabel;
	}

	@Override
	public String getHyperlinkText() {
		return TypeScriptUIMessages.TypeScriptHyperlink_text;
	}

	@Override
	public void open() {
		if (file != null) {
			EditorUtils.openInEditor(file, span);
		} else {
			EditorUtils.openInEditor(fsFile, span);
		}
	}

}
