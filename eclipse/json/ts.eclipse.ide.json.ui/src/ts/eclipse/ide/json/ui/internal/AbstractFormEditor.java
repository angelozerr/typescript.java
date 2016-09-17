package ts.eclipse.ide.json.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

public abstract class AbstractFormEditor extends FormEditor {

	private StructuredTextEditor jsonEditor;
	private int jsonEditorIndex;

	private final Map<Integer, AbstractFormPage> pageIndexes;

	public AbstractFormEditor() {
		pageIndexes = new HashMap<Integer, AbstractFormPage>();
	}

	@Override
	protected void addPages() {
		this.jsonEditor = new StructuredTextEditor();
		jsonEditor.setEditorPart(this);
		try {
			// Add pages like overview, etc
			doAddPages();
			// Add source page
			jsonEditorIndex = addPage(jsonEditor, getEditorInput());
			setPageText(jsonEditorIndex, "Source");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		jsonEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		jsonEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return jsonEditor.isSaveAsAllowed();
	}

	public IDocument getDocument() {
		IDocumentProvider provider = jsonEditor.getDocumentProvider();
		return provider.getDocument(getEditorInput());
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		T a = super.getAdapter(adapter);
		if (a == null) {
			// Ex: for Outline
			return (T) jsonEditor.getAdapter(adapter);
		}
		return a;
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		AbstractFormPage page = pageIndexes.get(newPageIndex);
		if (page != null) {
			page.updateUIBindings();
		}
	}

	public int addPage(AbstractFormPage page) throws PartInitException {
		int index = super.addPage(page);
		pageIndexes.put(index, page);
		return index;
	}

	protected abstract void doAddPages() throws PartInitException;
}
