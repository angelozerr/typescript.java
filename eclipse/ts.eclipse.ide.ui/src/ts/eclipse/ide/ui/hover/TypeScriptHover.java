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
package ts.eclipse.ide.ui.hover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

import ts.TypeScriptNoContentAvailableException;
import ts.client.quickinfo.QuickInfo;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;
import ts.eclipse.ide.ui.utils.HTMLTypeScriptPrinter;
import ts.eclipse.jface.text.html.TypeScriptBrowserInformationControlInput;
import ts.resources.ITypeScriptFile;
import ts.utils.StringUtils;

/**
 * TypeScript Hover.
 *
 */
public class TypeScriptHover extends AbstractTypeScriptHover implements ITypeScriptHoverInfoProvider {

	private IInformationControlCreator fHoverControlCreator;
	private IInformationControlCreator fPresenterControlCreator;
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
		if (TypeScriptResourceUtil.canConsumeTsserver(scriptFile)) {
			try {
				IProject project = scriptFile.getProject();
				tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
				int position = hoverRegion.getOffset();
				ITypeScriptFile tsFile = tsProject.openFile(scriptFile, textViewer.getDocument());

				QuickInfo quickInfo = tsFile.quickInfo(position).get(5000, TimeUnit.MILLISECONDS);
				String text = HTMLTypeScriptPrinter
						.getQuickInfo(quickInfo,
								TypeScriptUIPlugin.getDefault().getPreferenceStore()
										.getBoolean(TypeScriptUIPreferenceConstants.USE_TEXMATE_FOR_SYNTAX_COLORING)
												? scriptFile : null);
				return StringUtils.isEmpty(text) ? null : new TypeScriptBrowserInformationControlInput(null, text, 20);
			} catch (ExecutionException e) {
				if (e.getCause() instanceof TypeScriptNoContentAvailableException) {
					// Ignore "No content available" error.
					return null;
				}
				TypeScriptUIPlugin.log("Error while TypeScript hover", e);
			} catch (Exception e) {
				TypeScriptUIPlugin.log("Error while TypeScript hover", e);
			}
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fHoverControlCreator == null)
			fHoverControlCreator = new IDEHoverControlCreator(getInformationPresenterControlCreator(), this);
		return fHoverControlCreator;
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fPresenterControlCreator == null)
			fPresenterControlCreator = new IDEPresenterControlCreator(this);
		return fPresenterControlCreator;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		TypeScriptBrowserInformationControlInput info = (TypeScriptBrowserInformationControlInput) getHoverInfo2(
				textViewer, hoverRegion);
		return info != null ? info.getHtml() : null;
	}
}
