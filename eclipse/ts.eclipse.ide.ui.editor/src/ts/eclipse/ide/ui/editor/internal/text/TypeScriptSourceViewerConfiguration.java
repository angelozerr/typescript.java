package ts.eclipse.ide.ui.editor.internal.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.ui.text.IColorManager;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;

public class TypeScriptSourceViewerConfiguration extends JavaScriptSourceViewerConfiguration {

	public TypeScriptSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return null;
	}

}
