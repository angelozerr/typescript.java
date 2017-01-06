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
package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import java.lang.reflect.Constructor;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.completions.ICompletionEntryFactory;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * TypeScript {@link ICompletionProposal} implementation with JSDT
 * {@link IJavaCompletionProposal} to sort completions item with releveant.
 */
public class JSDTCompletionProposalFactory implements ICompletionEntryFactory {

	// ICompletionProposalExtension7 which is used for highlight completion item
	// is only available with Eclipse Neon, use reflection to use it.
	private static final Constructor<?> CONSTRUCTOR_EXTENSION7;

	static {
		CONSTRUCTOR_EXTENSION7 = getConstructorExtension7();
	}

	private static Constructor<?> getConstructorExtension7() {
		try {
			Class<?> clazz = JSDTCompletionProposalFactory.class.getClassLoader().loadClass(
					"ts.eclipse.ide.jsdt.internal.ui.editor.contentassist.JSDTTypeScriptCompletionProposalWithExtension7");
			return clazz.getConstructors()[0];
		} catch (Throwable e) {
			return null;
		}
	}

	private String prefix;
	private int position;

	public JSDTCompletionProposalFactory(int position, String prefix) {
		this.prefix = prefix;
		this.position = position;
	}

	@Override
	public CompletionEntry create(ICompletionEntryMatcher matcher, String fileName, int line, int offset,
			ITypeScriptServiceClient client) {
		try {
			if (CONSTRUCTOR_EXTENSION7 != null) {
				return (CompletionEntry) CONSTRUCTOR_EXTENSION7.newInstance(matcher, fileName, line, offset, client, position, prefix);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new JSDTTypeScriptCompletionProposal(matcher, fileName, line, offset, client, position, prefix);
	}

}
