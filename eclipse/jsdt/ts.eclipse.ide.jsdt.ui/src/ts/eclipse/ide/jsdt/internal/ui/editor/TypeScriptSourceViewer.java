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
package ts.eclipse.ide.jsdt.internal.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaSourceViewer;

/**
 * Extension of JSDT {@link JavaSourceViewer}.
 *
 */
public class TypeScriptSourceViewer extends JavaSourceViewer {

	public static final int OPEN_IMPLEMENTATION = 53;

	private IInformationPresenter implementationPresenter;

	public TypeScriptSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
	}

	/**
	 * Sets the given reconciler.
	 *
	 * @param reconciler
	 *            the reconciler
	 * 
	 */
	void setReconciler(IReconciler reconciler) {
		fReconciler = reconciler;
	}

	/**
	 * Returns the reconciler.
	 *
	 * @return the reconciler or <code>null</code> if not set
	 * 
	 */
	IReconciler getReconciler() {
		return fReconciler;
	}

	@Override
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset,
			int modelRangeLength) {
		// partial fix for:
		// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=1970
		// when our document is set, especially to null during close,
		// immediately uninstall the reconciler.
		// this is to avoid an unnecessary final "reconcile"
		// that blocks display thread
		if (document == null) {
			if (fReconciler != null) {
				fReconciler.uninstall();
			}
		}
		super.setDocument(document, annotationModel, modelRangeOffset, modelRangeLength);
	}

	@Override
	public void configure(SourceViewerConfiguration configuration) {
		super.configure(configuration);
		if (configuration instanceof TypeScriptSourceViewerConfiguration) {
			TypeScriptSourceViewerConfiguration tsConfiguration = (TypeScriptSourceViewerConfiguration) configuration;
			implementationPresenter = tsConfiguration.getImplementationPresenter(this);
			if (implementationPresenter != null)
				implementationPresenter.install(this);
		}
	}

	@Override
	public void doOperation(int operation) {
		if (getTextWidget() == null)
			return;

		switch (operation) {
		case OPEN_IMPLEMENTATION:
			if (implementationPresenter != null) {
				implementationPresenter.showInformation();
			}
			return;
		}
		super.doOperation(operation);
	}

	@Override
	public boolean canDoOperation(int operation) {
		if (operation == OPEN_IMPLEMENTATION) {
			return implementationPresenter != null;
		}
		return super.canDoOperation(operation);
	}
}
