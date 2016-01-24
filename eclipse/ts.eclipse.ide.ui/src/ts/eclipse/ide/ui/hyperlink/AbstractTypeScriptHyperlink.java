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
package ts.eclipse.ide.ui.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import ts.TSException;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.server.definition.ITypeScriptDefinitionCollector;
import ts.utils.StringUtils;

public abstract class AbstractTypeScriptHyperlink implements IHyperlink, ITypeScriptDefinitionCollector {

	protected final IRegion region;
	protected final IIDETypeScriptProject tsProject;

	private IFile file;
	private Integer startLine;
	private Integer startOffset;
	private Integer endLine;
	private Integer endOffset;

	public AbstractTypeScriptHyperlink(IRegion region, IIDETypeScriptProject tsProject) {
		this.region = region;
		this.tsProject = tsProject;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public void addDefinition(String filename, int startLine, int startOffset, int endLine, int endOffset)
			throws TSException {
		this.file = findFile(filename);
		this.startLine = startLine;
		this.startOffset = startOffset;
		this.endLine = endLine;
		this.endOffset = endOffset;

	}

	private IFile findFile(String path) {	
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		IPath filePath = new Path(path);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(filePath);
		if (file.exists()) {
			return file;
		}
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(filePath);
		if (files.length > 0) {
			return files[0];
		}
		return null;
	}

	@Override
	public final void open() {
		IFile file = getFile();
		Integer startLine = getStartLine();
		Integer startOffset = getStartOffset();
		Integer endLine = getEndLine();
		Integer endOffset = getEndOffset();
		if (file != null && file.exists()) {
			EditorUtils.openInEditor(file, startLine, startOffset, endLine, endOffset, true);
		}
	}

	/**
	 * Execute with async mode the tern "definition" query and returns true if
	 * the file was found.
	 * 
	 * @return
	 */
	public boolean isValid() {
		try {
			findDef();
		} catch (Exception e) {
			return false;
		}
		return file != null && file.exists();
	}

	public IFile getFile() {
		return file;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public Integer getStartOffset() {
		return startOffset;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	/**
	 * Execute the TypeScript "definition" query.
	 * 
	 * @throws Exception
	 */
	protected abstract void findDef() throws Exception;
}
