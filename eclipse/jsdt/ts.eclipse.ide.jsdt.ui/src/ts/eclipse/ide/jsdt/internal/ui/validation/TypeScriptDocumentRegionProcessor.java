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
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.wst.sse.ui.internal.reconcile.DocumentRegionProcessor;

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

	public TypeScriptDocumentRegionProcessor(IResource resource) {
		this.contentType = getContentType(resource);
	}

	private String getContentType(IResource resource) {
		String extension = resource.getFileExtension();
		if (FileUtils.JS_EXTENSION.equals(extension)) {
			return "org.eclipse.wst.jsdt.core.jsSource";
		}
		return new StringBuilder("ts.eclipse.ide.jsdt.core.").append(extension).append("Source").toString();
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

}
