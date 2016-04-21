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
package ts.eclipse.ide.jsdt.internal.ui.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.wst.sse.ui.internal.reconcile.DocumentRegionProcessor;

import ts.eclipse.ide.ui.folding.IndentFoldingStrategy;
import ts.utils.FileUtils;

/**
 * Extends SSE {@link DocumentRegionProcessor} to be able to validate JSDT
 * Editor content with "org.eclipse.wst.sse.ui.sourcevalidation" (see
 * tern.eclipse.ide.linter.ui/plugin.xml) since JSDT Editor have not an SSE
 * IStructuredModel.
 * 
 */
public class TypeScriptDocumentRegionProcessor extends DocumentRegionProcessor {

	private final String contentType;
	private IndentFoldingStrategy foldingStrategy;

	public TypeScriptDocumentRegionProcessor(IResource resource) {
		this.contentType = getContentType(resource);
	}

	private String getContentType(IResource resource) {
		String extension = resource.getFileExtension();
		if (FileUtils.JS_EXTENSION.equals(extension)) {
			return "org.eclipse.wst.jsdt.core.jsSource";
		}
		return new StringBuilder("ts.eclipse.ide.core.").append(extension).append("Source").toString();
	}

	@Override
	public synchronized void startReconciling() {
		super.startReconciling();
	}

	@Override
	protected IReconcilingStrategy getSpellcheckStrategy() {
		// don't use SSE spelling strategy.
		return null;
	}

	@Override
	protected String getContentType(IDocument doc) {
		return contentType;
	}

	@Override
	public void setDocument(IDocument doc) {
		super.setDocument(doc);
		if (foldingStrategy != null) {
			foldingStrategy.uninstall();
		}
		foldingStrategy = null;
	}

	protected IReconcilingStrategy getTypeScriptFoldingStrategy() {
		if ("org.eclipse.wst.jsdt.core.jsSource".equals(contentType)) {
			return super.getFoldingStrategy();
		}
		if (foldingStrategy == null) {
			foldingStrategy = new IndentFoldingStrategy();
			foldingStrategy.setViewer((ProjectionViewer) getTextViewer());
			foldingStrategy.setDocument(getDocument());
		}
		return foldingStrategy;
	}

	/**
	 * Override process method to call folding strategy BEFORE validation which
	 * can take time.
	 */
	@Override
	protected void process(DirtyRegion dirtyRegion) {
		if (!isInstalled() /* || isInRewriteSession() */ || dirtyRegion == null || getDocument() == null)
			return;

		/*
		 * if there is a folding strategy then reconcile it for the entire dirty
		 * region. NOTE: the folding strategy does not care about the sub
		 * regions.
		 */
		if (getTypeScriptFoldingStrategy() != null) {
			getTypeScriptFoldingStrategy().reconcile(dirtyRegion, null);
		}

		super.process(dirtyRegion);
	}

	/**
	 * Override setEntireDocumentDirty method to call folding strategy BEFORE
	 * validation which can take time.
	 */
	@Override
	protected void setEntireDocumentDirty(IDocument document) {

		// make the entire document dirty
		// this also happens on a "save as"
		if (document != null && isInstalled() && document.getLength() == 0) {

			// if there is a folding strategy then reconcile it
			if (getTypeScriptFoldingStrategy() != null) {
				getTypeScriptFoldingStrategy().reconcile(new Region(0, document.getLength()));
			}
		}
		super.setEntireDocumentDirty(document);
	}
}
