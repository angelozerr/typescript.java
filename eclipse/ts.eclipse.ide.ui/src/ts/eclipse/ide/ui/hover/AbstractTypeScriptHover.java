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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.JavaWordFinder;
import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * Abstract class for TypeScript Hover.
 *
 */
public abstract class AbstractTypeScriptHover
		implements ITextHover, ITextHoverExtension, ITextHoverExtension2, IInformationProviderExtension2 {

	private IEditorPart editor;

	public IEditorPart getEditor() {
		return editor;
	}

	public void setEditor(IEditorPart editor) {
		this.editor = editor;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		return getHoverInfo(textViewer, hoverRegion);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return JavaWordFinder.findWord(textViewer.getDocument(), offset);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell shell) {
				return new DefaultInformationControl(shell, true);
			}
		};
	}

	protected IFile getFile(ITextViewer textViewer) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			return EditorUtils.getFile(editor);
		}
		return TypeScriptResourceUtil.getFile(textViewer.getDocument());
	}

}
