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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.TypeScriptNoContentAvailableException;
import ts.client.FileSpan;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.ui.JavaWordFinder;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * TypeScript Hyperlink detector.
 *
 */
public class TypeScriptHyperLinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		// Get resource from the given text viewer.
		IResource resource = getResource(textViewer);
		if (resource == null) {
			return null;
		}
		if (TypeScriptResourceUtil.canConsumeTsserver(resource)) {
			// the project of the resource has typescript nature, execute
			// typescript
			// hyperlink.
			try {
				IProject project = resource.getProject();
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
				IDocument document = textViewer.getDocument();
				IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);
				IRegion wordRegion = JavaWordFinder.findWord(document, region.getOffset());
				if (wordRegion == null) {
					return null;
				}
				// Consume tsserver "definition" command and create hyperlink
				// file span are found.
				List<FileSpan> spans = tsFile.definition(wordRegion.getOffset()).get(5000, TimeUnit.MILLISECONDS);
				return createHyperlinks(spans, wordRegion);
			} catch (ExecutionException e) {
				if (e.getCause() instanceof TypeScriptNoContentAvailableException) {
					// Ignore "No content available" error.
					return null;
				}
				TypeScriptUIPlugin.log("Error while TypeScript hyperlink", e);
			} catch (Exception e) {
				TypeScriptUIPlugin.log("Error while TypeScript hyperlink", e);
			}
		}
		return null;
	}

	/**
	 * Create HyperLink list from the given TypeScript file spans.
	 * 
	 * @param spans
	 * @param region
	 * @return
	 */
	private IHyperlink[] createHyperlinks(List<FileSpan> spans, IRegion region) {
		if (spans == null || spans.size() < 1) {
			return null;
		}
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
		for (FileSpan span : spans) {
			IHyperlink hyperlink = createHyperLink(span, region);
			if (hyperlink != null) {
				hyperlinks.add(hyperlink);
			}
		}
		return hyperlinks.size() > 0 ? hyperlinks.toArray(new IHyperlink[hyperlinks.size()]) : null;
	}

	/**
	 * Create Hyperlink from the given TypeScript file span and null if file is
	 * not found.
	 * 
	 * @param span
	 * @param region
	 * @return
	 */
	private IHyperlink createHyperLink(FileSpan span, IRegion region) {
		IFile file = WorkbenchResourceUtil.findFileFromWorkspace(span.getFile());
		if (file != null) {
			return new TypeScriptHyperlink(file, span, region);
		}
		File fsFile = WorkbenchResourceUtil.findFileFormFileSystem(span.getFile());
		if (fsFile != null) {
			return new TypeScriptHyperlink(fsFile, span, region);
		}
		return null;
	}

	/**
	 * Returns the {@link IResource} from the given text viewer and null
	 * otherwise.
	 * 
	 * @param textViewer
	 * @return the {@link IResource} from the given text viewer and null
	 *         otherwise.
	 */
	private IResource getResource(ITextViewer textViewer) {
		ITextEditor textEditor = (ITextEditor) getAdapter(ITextEditor.class);
		if (textEditor != null) {
			return EditorUtils.getResource(textEditor);
		}
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(textViewer.getDocument());
		if (textFileBuffer != null) {
			IPath location = textFileBuffer.getLocation();
			return ResourcesPlugin.getWorkspace().getRoot().findMember(location);
		}
		return null;
	}

}
