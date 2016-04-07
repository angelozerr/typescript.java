package ts.eclipse.ide.ui.editor.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaSourceViewer;

public class TypeScriptSourceViewer extends JavaSourceViewer {

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
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int modelRangeOffset, int modelRangeLength) {
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
}
