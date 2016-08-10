package ts.eclipse.ide.jsdt.internal.ui.template.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal;

import ts.eclipse.ide.jsdt.core.template.TypeScriptContextType;

public class TemplateEngine {
	/** The context type. */
	private TemplateContextType fContextType;
	/** The result proposals. */
	private ArrayList fProposals = new ArrayList();
	/** Positions created on the key documents to remove in reset. */
	private final Map fPositions = new HashMap();

	/**
	 * Creates the template engine for a particular context type Se
	 * <code>TemplateContext</code> for supported context types.
	 */
	public TemplateEngine(TemplateContextType contextType) {
		Assert.isNotNull(contextType);
		fContextType = contextType;
	}

	public void complete(ITextViewer viewer, int completionPosition) {
		IDocument document= viewer.getDocument();

		if (!(fContextType instanceof TypeScriptContextType))
			return;

		Point selection= viewer.getSelectedRange();
		Position position= new Position(completionPosition, selection.y);

		// remember selected text
		String selectedText= null;
		if (selection.y != 0) {
			try {
				selectedText= document.get(selection.x, selection.y);
				document.addPosition(position);
				fPositions.put(document, position);
			} catch (BadLocationException e) {}
		}

	}

	public void reset() {
		fProposals.clear();
		for (Iterator it = fPositions.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			IDocument doc = (IDocument) entry.getKey();
			Position position = (Position) entry.getValue();
			doc.removePosition(position);
		}
		fPositions.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public TemplateProposal[] getResults() {
		return (TemplateProposal[]) fProposals.toArray(new TemplateProposal[fProposals.size()]);
	}

}
