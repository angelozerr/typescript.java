package ts.eclipse.ide.ui.outline;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.eclipse.ide.ui.JavaWordFinder;

public class TypeScriptElementProvider implements IInformationProvider, IInformationProviderExtension {

	/**
	 * Current editor
	 */
	private ITextEditor editor;

	/**
	 * Constructor
	 * 
	 * @param editor
	 *            current editor
	 */
	public TypeScriptElementProvider(ITextEditor editor) {
		this.editor = editor;
	}

	@Override
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		// Calls setInput on the quick outline popup dialog
		if ((textViewer == null) || (editor == null)) {
			return null;
		}

		Object selection;
		selection = editor.getSelectionProvider().getSelection();

		// If the input is null, then the dialog does not open
		// Define an empty object for no selection instead of null
		if (selection == null) {
			selection = new Object();
		}
		return selection;
	}

	@Override
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		// Subject used in getInformation2
		if ((textViewer == null) || (editor == null)) {
			return null;
		}
		// Get the selected region
		IRegion region = JavaWordFinder.findWord(textViewer.getDocument(), offset);
		// Ensure the region is defined. Define an empty one if it is not.
		if (region == null) {
			return new Region(offset, 0);
		}
		return region;
	}

	/**
	 * @param textViewer
	 *            the viewer in whose document the subject is contained
	 * @param subject
	 *            the text region constituting the information subject
	 * @return the information about the subject
	 * @deprecated
	 */
	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return getInformation2(textViewer, subject).toString();
	}

}
