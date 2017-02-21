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
package ts.eclipse.ide.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ts.client.codefixes.CodeAction;
import ts.client.codefixes.FileCodeEdits;
import ts.eclipse.ide.core.utils.DocumentUtils;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.utils.StringUtils;

/**
 * TypeScript {@link CodeAction} completion proposal.
 *
 */
public class CodeActionCompletionProposal implements ICompletionProposal {

	private final CodeAction codeAction;
	private final String fileName;

	public CodeActionCompletionProposal(CodeAction codeAction, String fileName) {
		this.codeAction = codeAction;
		this.fileName = fileName;
	}

	@Override
	public void apply(IDocument document) {
		for (FileCodeEdits codeEdits : codeAction.getChanges()) {
			apply(codeEdits, document);
		}
	}

	private void apply(FileCodeEdits codeEdits, IDocument document) {
		IDocument documentToUpdate = getDocumentToUpdate(codeEdits.getFileName(), document);
		if (documentToUpdate != null) {
			try {
				DocumentUtils.applyEdits(documentToUpdate, codeEdits.getTextChanges());
			} catch (Exception e) {
				TypeScriptUIPlugin.log(e);
			}
		}
	}

	private IDocument getDocumentToUpdate(String fileName, IDocument document) {
		if (StringUtils.isEmpty(fileName) || this.fileName.equals(fileName)) {
			// Update the document of the fileName which have opened the
			// QuickFix
			return document;
		}
		IFile file = WorkbenchResourceUtil.findFileFromWorkspace(fileName);
		if (file != null && file.exists()) {
			EditorUtils.openInEditor(file, true);
			return TypeScriptResourceUtil.getDocument(file);
		}
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return codeAction.getDescription();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

}
