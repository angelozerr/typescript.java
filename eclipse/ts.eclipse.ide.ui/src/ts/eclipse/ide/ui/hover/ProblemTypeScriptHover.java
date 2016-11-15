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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;

import ts.client.CommandNames;
import ts.client.codefixes.CodeAction;
import ts.client.codefixes.ITypeScriptGetCodeFixesCollector;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.utils.StringUtils;

/**
 * Problem Hover used to display errors when mouse over a JS content which have
 * a TypeScript error.
 *
 */
public class ProblemTypeScriptHover extends AbstractAnnotationHover {

	protected static class ProblemInfo extends AnnotationInfo {

		private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];

		public ProblemInfo(Annotation annotation, Position position, ITextViewer textViewer) {
			super(annotation, position, textViewer);
		}

		@Override
		public ICompletionProposal[] getCompletionProposals() {
			IDocument document = viewer.getDocument();
			IFile file = TypeScriptResourceUtil.getFile(document);
			try {
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(file.getProject());
				if (tsProject.canSupport(CommandNames.GetCodeFixes)) {
					// Get code fixes with TypeScript 2.1.1
					ITypeScriptFile tsFile = tsProject.openFile(file, document);
					List<String> errorCodes = createErrorCodes(tsProject);
					if (errorCodes != null) {
						final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
						tsFile.getCodeFixes(position.getOffset(), position.getOffset() + position.getLength(),
								errorCodes.toArray(StringUtils.EMPTY_STRING), new ITypeScriptGetCodeFixesCollector() {
									@Override
									public void fix(List<CodeAction> codeActions) {
										for (CodeAction codeAction : codeActions) {
											proposals.add(new CodeActionCompletionProposal(codeAction));
										}
									}
								});
						return proposals.toArray(new ICompletionProposal[proposals.size()]);
					}
					return NO_PROPOSALS;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return NO_PROPOSALS;
		}

		private List<String> createErrorCodes(ITypeScriptProject tsProject) {
			List<String> errorCodes = null;
			try {
				Method getAttributesMethod = annotation.getClass().getMethod("getAttributes", new Class[0]);
				Map getAttributes = (Map) getAttributesMethod.invoke(annotation, new Object[0]);
				Integer tsCode = (Integer) getAttributes.get("tsCode");
				if (tsCode != null) {
					String errorCode = String.valueOf(tsCode);
					if (tsProject.canFix(errorCode)) {
						if (errorCodes == null) {
							errorCodes = new ArrayList<String>();
						}
						errorCodes.add(errorCode);
					}
				}
			} catch (Throwable e) {
				TypeScriptUIPlugin.log("Error while getting TypeScript error code", e);
			}
			return errorCodes;

		}
	}

	public ProblemTypeScriptHover() {
		super(false);
	}

	@Override
	protected AnnotationInfo createAnnotationInfo(Annotation annotation, Position position, ITextViewer textViewer) {
		return new ProblemInfo(annotation, position, textViewer);
	}

}
