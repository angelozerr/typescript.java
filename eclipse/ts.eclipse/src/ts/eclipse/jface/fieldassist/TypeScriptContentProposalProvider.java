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

package ts.eclipse.jface.fieldassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ts.TypeScriptException;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.utils.TypeScriptHelper;

/**
 * {@link IContentProposalProvider} implementation to collect TypeScript
 * completion entries.
 */
public class TypeScriptContentProposalProvider implements IContentProposalProvider {

	private final String fileName;
	private final ITypeScriptProject tsProject;

	public TypeScriptContentProposalProvider(String fileName, ITypeScriptProject project) {
		this.fileName = fileName;
		this.tsProject = project;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		ITypeScriptFile tsFile = tsProject.getOpenedFile(fileName);
		String prefix = TypeScriptHelper.getPrefix(contents, position);
		ContentProposalCollector collector = new ContentProposalCollector(prefix,
				tsProject.getProjectSettings().getCompletionEntryMatcher());
		try {
			tsFile.completions(position, collector);
		} catch (TypeScriptException e) {
			e.printStackTrace();
		}
		return collector.getProposals().toArray(ContentProposalCollector.EMPTY_PROPOSAL);
	}

	public static List<Integer> readLines(final String input) {
		final List<Integer> list = new ArrayList<Integer>();
		try {
			final BufferedReader reader = new BufferedReader(new StringReader(input));
			String line = reader.readLine();
			while (line != null) {
				if (list.size() > 0) {
					list.add(line.length());// "\r\n".length());
				} else {
					list.add(line.length());
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
