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
package ts.eclipse.ide.ui.hyperlink;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import ts.TypeScriptException;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.utils.StringUtils;

/**
 * Abstract class for TypeScript Hyperlink.
 *
 */
public abstract class AbstractTypeScriptHyperlink implements IHyperlink, ITypeScriptDefinitionCollector {

	protected final IRegion region;
	protected final IIDETypeScriptProject tsProject;

	private IFile file;
	private File fs;
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
			throws TypeScriptException {
		this.file = findFileFromWorkspace(filename);
		if (this.file == null) {
			this.fs = findFileFormFileSystem(filename);
		}
		this.startLine = startLine;
		this.startOffset = startOffset;
		this.endLine = endLine;
		this.endOffset = endOffset;

	}

	private IFile findFileFromWorkspace(String path) {
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
			file = files[0];
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	private File findFileFormFileSystem(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		File file = new File(path);
		return (file.exists() && file.isFile()) ? file : null;
	}

	@Override
	public final void open() {
		Integer startLine = getStartLine();
		Integer startOffset = getStartOffset();
		Integer endLine = getEndLine();
		Integer endOffset = getEndOffset();

		if (isFileExists(file)) {
			EditorUtils.openInEditor(file, startLine, startOffset, endLine, endOffset, true);
		} else if (isFileExists(fs)) {
			EditorUtils.openInEditor(fs, startLine, startOffset, endLine, endOffset, true);
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
		return isFileExists(file) || isFileExists(fs);
	}

	private boolean isFileExists(IFile file) {
		if (file == null) {
			return false;
		}
		return file.exists();
	}

	private boolean isFileExists(File file) {
		if (file == null) {
			return false;
		}
		return file.exists() && file.isFile();
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
