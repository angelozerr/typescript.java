package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;

import ts.eclipse.ide.ui.utils.EditorUtils;

public class TypeScriptContentAssistInvocationContext extends ContentAssistInvocationContext {

	private IEditorPart editor;

	public TypeScriptContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor) {
		super(viewer, offset);
		this.editor = editor;
	}

	public IEditorPart getEditor() {
		return editor;
	}

	public IResource getResource() {
		return EditorUtils.getResource(editor);
	}

}
