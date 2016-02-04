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
package ts.eclipse.ide.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.Trace;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.eclipse.jface.text.html.TypeScriptBrowserInformationControlInput;
import ts.resources.ITypeScriptFile;
import ts.utils.StringUtils;

/**
 * TypeScript Hover.
 *
 */
public class TypeScriptHover extends AbstractTypeScriptHover  {

	private IIDETypeScriptProject tsProject;
	private Integer offset;
	private ITypeScriptFile file;

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		this.tsProject = null;
		this.offset = null;

		IFile scriptFile = getFile(textViewer);
		if (scriptFile == null) {
			return null;
		}
		IProject project = scriptFile.getProject();
		if (TypeScriptCorePlugin.hasTypeScriptNature(project)) {
			try {
				tsProject = TypeScriptCorePlugin.getTypeScriptProject(project);
				int position = hoverRegion.getOffset();
				ITypeScriptFile tsFile = tsProject.openFile(scriptFile, textViewer.getDocument());

				HTMLTypeScriptQuickInfoCollector collector = new HTMLTypeScriptQuickInfoCollector();
				tsProject.quickInfo(tsFile, position, collector);

				String text = collector.getInfo();
				return StringUtils.isEmpty(text) ? null : new TypeScriptBrowserInformationControlInput(null, text, 20);
			} catch (Exception e) {
				Trace.trace(Trace.WARNING, "Error while TypeScript hover", e);
			}
		}
		return null;
	}

	protected IFile getFile(ITextViewer textViewer) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			return EditorUtils.getFile(editor);
		}

		return EditorUtils.getFile(textViewer.getDocument());
	}

}
