package ts.eclipse.ide.jsdt.internal.ui.text;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptTextTools;

import ts.eclipse.ide.jsdt.internal.ui.text.jsx.IJSXPartitions;

public class TypeScriptTextTools extends JavaScriptTextTools {

	/**
	 * Array with legal content types.
	 * 
	 */
	private final static String[] LEGAL_CONTENT_TYPES = new String[] { IJavaScriptPartitions.JAVA_DOC,
			IJavaScriptPartitions.JAVA_MULTI_LINE_COMMENT, IJavaScriptPartitions.JAVA_SINGLE_LINE_COMMENT,
			IJavaScriptPartitions.JAVA_STRING, IJavaScriptPartitions.JAVA_CHARACTER,
			IJavaScriptPartitions.JAVASCRIPT_TEMPLATE_LITERAL, IJSXPartitions.JSX };

	public TypeScriptTextTools(IPreferenceStore store, Preferences coreStore) {
		super(store, coreStore);
	}

	@Override
	public IDocumentPartitioner createDocumentPartitioner() {
		return new FastPartitioner(getPartitionScanner(), LEGAL_CONTENT_TYPES);
	}

	@Override
	public IPartitionTokenScanner getPartitionScanner() {
		return new FastTypeScriptPartitionScanner();
	}

}
