package ts.eclipse.ide.ui.editor.internal;

import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;

public class TypeScriptDocumentProvider extends TextFileDocumentProvider {

	public TypeScriptDocumentProvider() {
		IDocumentProvider provider = new TextFileDocumentProvider();
		provider = new ForwardingDocumentProvider(IJavaScriptPartitions.JAVA_PARTITIONING,
				new JavaDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);
	}
}
