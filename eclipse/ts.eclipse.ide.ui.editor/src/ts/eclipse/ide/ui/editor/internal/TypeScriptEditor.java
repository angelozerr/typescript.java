package ts.eclipse.ide.ui.editor.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;
import org.eclipse.wst.jsdt.ui.text.JavaScriptTextTools;

import ts.eclipse.ide.ui.editor.TypeScriptUIEditorPlugin;

public class TypeScriptEditor extends AbstractDecoratedTextEditor {

	public TypeScriptEditor() {
		super.setDocumentProvider(TypeScriptUIEditorPlugin.getDefault().getTypeScriptDocumentProvider());
	}

	@Override
	protected void initializeEditor() {
		IPreferenceStore store= createCombinedPreferenceStore(null);
		setPreferenceStore(store);
		setSourceViewerConfiguration(createJavaSourceViewerConfiguration());

		super.initializeEditor();
	}

	/**
	 * Returns a new Java source viewer configuration.
	 * 
	 * @return a new <code>JavaScriptSourceViewerConfiguration</code>
	 * 
	 */
	protected JavaScriptSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		JavaScriptTextTools textTools = JavaScriptPlugin.getDefault().getJavaTextTools();
		return new JavaScriptSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this,
				IJavaScriptPartitions.JAVA_PARTITIONING);
	}
	
	/**
	 * Creates and returns the preference store for this Java editor with the given input.
	 *
	 * @param input The editor input for which to create the preference store
	 * @return the preference store for this editor
	 *
	 * 
	 */
	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List stores= new ArrayList();

//		IJavaScriptProject project= EditorUtility.getJavaProject(input);
//		if (project != null) {
//			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project.getProject()), JavaScriptCore.PLUGIN_ID));
//		}

		stores.add(JavaScriptPlugin.getDefault().getPreferenceStore());
//		stores.add(new PreferencesAdapter(JavaScriptCore.getPlugin().getPluginPreferences()));
		stores.add(EditorsUI.getPreferenceStore());

		return new ChainedPreferenceStore((IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores.size()]));
	}

}
