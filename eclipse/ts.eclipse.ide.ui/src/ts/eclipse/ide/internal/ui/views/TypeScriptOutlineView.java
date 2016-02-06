package ts.eclipse.ide.internal.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.views.contentoutline.ContentOutline;
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
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.Trace;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.TypeScriptResourcesManager;

/**
 * TypeScript outline view.
 *
 */
public class TypeScriptOutlineView extends ContentOutline {

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		// Try to get an outline page.
		IContentOutlinePage page = getOutlinePage(part);
		if (page != null) {
			if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}
		// There is no content outline
		return null;
	}

	private IContentOutlinePage getOutlinePage(IWorkbenchPart part) {
		if (part != null && part instanceof IEditorPart) {
			IFile file = EditorUtils.getFile((IEditorPart) part);
			if (file != null && TypeScriptResourcesManager.isTSFile(file)) {
				IProject project = file.getProject();
				if (TypeScriptCorePlugin.hasTypeScriptNature(project)) {
					IDocument document = EditorUtils.getDocument(file);
					if (document != null) {
						try {
							IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(project);
							IIDETypeScriptFile tsFile = tsProject.openFile(file, document);
							return new TypeScriptContentOutlinePage(tsFile);
						} catch (Throwable e) {
							Trace.trace(Trace.SEVERE, "Error while opening TypeScript outline", e);
						}
					}
				}
			}
		}
		return null;
	}

}