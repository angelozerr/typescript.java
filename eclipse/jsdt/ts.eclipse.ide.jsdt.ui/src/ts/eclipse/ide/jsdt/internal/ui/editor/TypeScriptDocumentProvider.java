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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
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
	
	@Override
	protected DocumentProviderOperation createSaveOperation(Object element, IDocument document, boolean overwrite)
			throws CoreException {
		// TODO Auto-generated method stub
		return super.createSaveOperation(element, document, overwrite);
	}
}
