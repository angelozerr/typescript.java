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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ts.client.codefixes.CodeAction;
import ts.eclipse.ide.ui.utils.EditorUtils;

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
		EditorUtils.applyEdit(codeAction.getChanges(), document, fileName);
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
