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
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;

import ts.client.CommandNames;
import ts.client.codefixes.CodeAction;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;

/**
 * Problem Hover used to display errors when mouse over a JS content which have
 * a TypeScript error.
 *
 */
public class ProblemTypeScriptHover extends AbstractAnnotationHover {

	protected static class ProblemInfo extends AnnotationInfo {

		private static final Class<?>[] EMPTY_CLASS = new Class[0];
		private static final Object[] EMPTY_OBJECT = new Object[0];
		private static final String GET_ATTRIBUTES_METHOD_NAME = "getAttributes";
		
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
					List<Integer> errorCodes = createErrorCodes(tsProject);
					if (errorCodes != null) {
						final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
						List<CodeAction> codeActions = tsFile.getCodeFixes(position.getOffset(),
								position.getOffset() + position.getLength(), errorCodes)
								.get(5000, TimeUnit.MILLISECONDS);
						for (CodeAction codeAction : codeActions) {
							proposals.add(new CodeActionCompletionProposal(codeAction));
						}
						return proposals.toArray(new ICompletionProposal[proposals.size()]);
					}
					return NO_PROPOSALS;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return NO_PROPOSALS;
		}

		private List<Integer> createErrorCodes(ITypeScriptProject tsProject) {
			List<Integer> errorCodes = null;
			try {
				// Try to retrieve the TypeScript error code from the SSE
				// TemporaryAnnotation.
				Method getAttributesMethod = annotation.getClass().getMethod(GET_ATTRIBUTES_METHOD_NAME, EMPTY_CLASS);
				Map getAttributes = (Map) getAttributesMethod.invoke(annotation, EMPTY_OBJECT);
				Integer tsCode = (Integer) getAttributes.get("tsCode");
				if (tsCode != null) {
					Integer errorCode = tsCode;
					if (tsProject.canFix(errorCode)) {
						if (errorCodes == null) {
							errorCodes = new ArrayList<Integer>();
						}
						errorCodes.add(errorCode);
					}
				}
			} catch (NoSuchMethodException e) {
				// The annotation is not a
				// org.eclipse.wst.sse.ui.internal.reconcile.TemporaryAnnotation
				// ignore the error.
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
