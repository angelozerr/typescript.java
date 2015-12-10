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
package ts.eclipse.ide.jsdt.internal.ui.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.wst.sse.ui.internal.reconcile.DocumentRegionProcessor;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.resources.ITypeScriptFile;

/**
 * Extends SSE {@link DocumentRegionProcessor} to be able to validate JSDT
 * Editor content with "org.eclipse.wst.sse.ui.sourcevalidation" (see
 * tern.eclipse.ide.linter.ui/plugin.xml) since JSDT Editor have not an SSE
 * IStructuredModel.
 * 
 */
public class TypeScriptDocumentRegionProcessor extends DocumentRegionProcessor {

	private final IResource resource;

	public TypeScriptDocumentRegionProcessor(IResource resource) {
		this.resource = resource;
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
		return "ts.eclipse.ide.jsdt.core.tsSource";
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		super.process(dirtyRegion);
		try {
			IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(resource.getProject());
			if (tsProject != null) {
				ITypeScriptFile tsFile = tsProject.openFile(resource, getDocument());
				int start = dirtyRegion.getOffset();
				int end = start + dirtyRegion.getLength() - 1;
				String newText = dirtyRegion.getText();
				// It doesn't works, why?
				// tsProject.changeFile(tsFile, start, end, newText);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
