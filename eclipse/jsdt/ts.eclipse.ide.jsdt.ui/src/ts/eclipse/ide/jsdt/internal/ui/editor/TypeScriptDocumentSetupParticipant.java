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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.internal.ui.text.TypeScriptTextTools;

/**
 * The document setup participant for TypeScript.
 */
public class TypeScriptDocumentSetupParticipant implements IDocumentSetupParticipant {

	public TypeScriptDocumentSetupParticipant() {
	}

	@Override
	public void setup(IDocument document) {
		TypeScriptTextTools tools = JSDTTypeScriptUIPlugin.getDefault().getJavaTextTools();
		tools.setupJavaDocumentPartitioner(document, IJavaScriptPartitions.JAVA_PARTITIONING);
	}
}
