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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.provisional.codelens.CodeLensStrategy;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.wst.sse.ui.internal.reconcile.DocumentRegionProcessor;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ValidatorStrategy;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.core.JSDTTypeScriptCorePlugin;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptFoldingStrategy;
import ts.eclipse.ide.jsdt.internal.ui.editor.codelens.TypeScriptCodeLensStrategy;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.folding.IndentFoldingStrategy;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;
import ts.utils.FileUtils;

/**
 * Extends SSE {@link DocumentRegionProcessor} to be able to validate JSDT
 * Editor content with "org.eclipse.wst.sse.ui.sourcevalidation" (see
 * tern.eclipse.ide.linter.ui/plugin.xml) since JSDT Editor have not an SSE
 * IStructuredModel.
 * 
 */
public class TypeScriptDocumentRegionProcessor extends DocumentRegionProcessor {

	private final IResource resource;
	private final String contentType;
	private IndentFoldingStrategy foldingStrategy;
	private CodeLensStrategy codeLensStrategy;

	public TypeScriptDocumentRegionProcessor(IResource resource) {
		this.contentType = getContentType(resource);
		this.resource = resource;
	}

	@Override
	protected ValidatorStrategy getValidatorStrategy() {
		if (JSDTTypeScriptCorePlugin.getDefault().isJSDT2()) {
			// JSDT 2.0.0 provides the capability to define WTP Validator,
			// return null
			// to avoid validate twice
			return null;
		}
		return super.getValidatorStrategy();
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
			ITextViewer viewer = getTextViewer();
			if (viewer instanceof ProjectionViewer) {
				foldingStrategy = new TypeScriptFoldingStrategy();
				foldingStrategy.setViewer((ProjectionViewer) viewer);
				foldingStrategy.setDocument(getDocument());
			}
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

	@Override
	protected void endProcessing() {
		super.endProcessing();
		// Refresh navigation bar/tree used for outline
		if (resource != null) {
			try {
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(resource.getProject());
				if (tsProject != null) {
					IIDETypeScriptFile tsFile = tsProject.getOpenedFile(resource);
					if (tsFile != null) {
						tsFile.refreshNavBar();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (getCodeLensStrategy() != null) {
			getCodeLensStrategy().reconcile(null, null);
		}
	}

	protected IReconcilingStrategy getCodeLensStrategy() {
		if (!TypeScriptUIPlugin.getDefault().getPreferenceStore()
				.getBoolean(TypeScriptUIPreferenceConstants.EDITOR_ACTIVATE_CODELENS)) {
			return null;
		}
		if (codeLensStrategy == null && getDocument() != null) {
			if (getTextViewer() instanceof ISourceViewer) {
				ISourceViewer viewer = (ISourceViewer) getTextViewer();
				codeLensStrategy = new TypeScriptCodeLensStrategy(viewer);
				codeLensStrategy.setDocument(getDocument());
			}
		}
		return codeLensStrategy;
	}
}
