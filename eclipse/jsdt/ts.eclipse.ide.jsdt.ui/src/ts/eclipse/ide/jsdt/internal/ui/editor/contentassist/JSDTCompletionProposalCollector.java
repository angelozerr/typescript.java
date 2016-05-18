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
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.jface.text.contentassist.CompletionProposalCollector;

/**
 * TypeScript {@link ICompletionProposal} implementation with JSDT
 * {@link IJavaCompletionProposal} to sort completions item with releveant.
 */
public class JSDTCompletionProposalCollector extends CompletionProposalCollector {

	// ICompletionProposalExtension7 which is used for highlight completion item
	// is only available with Eclipse Neon, use reflection to use it.
	private static final Constructor<?> CONSTRUCTOR_EXTENSION7;

	static {
		CONSTRUCTOR_EXTENSION7 = getConstructorExtension7();
	}

	private static Constructor<?> getConstructorExtension7() {
		try {
			Class<?> clazz = JSDTCompletionProposalCollector.class.getClassLoader().loadClass(
					"ts.eclipse.ide.jsdt.internal.ui.editor.contentassist.JSDTTypeScriptCompletionProposalWithExtension7");
			return clazz.getConstructors()[0];
		} catch (Throwable e) {
			return null;
		}
	}

	public JSDTCompletionProposalCollector(int position, String prefix, ICompletionEntryMatcher matcher) {
		super(position, prefix, matcher);
	}

	@Override
	protected ICompletionEntry createEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		try {
			if (CONSTRUCTOR_EXTENSION7 != null) {
				return (ICompletionEntry) CONSTRUCTOR_EXTENSION7.newInstance(name, kind, kindModifiers, sortText,
						getPosition(), getPrefix(), fileName, line, offset, getMatcher(), client);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new JSDTTypeScriptCompletionProposal(name, kind, kindModifiers, sortText, getPosition(), getPrefix(),
				fileName, line, offset, getMatcher(), client);
	}

}
