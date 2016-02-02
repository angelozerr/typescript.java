package ts.eclipse.jface.fieldassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ts.TSException;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.utils.TSHelper;

public class TypeScriptContentProposalProvider implements IContentProposalProvider {

	private final String fileName;
	private final ITypeScriptProject project;

	public TypeScriptContentProposalProvider(String fileName, ITypeScriptProject project) {
		this.fileName = fileName;
		this.project = project;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		ITypeScriptFile tsFile = project.getOpenedFile(fileName);
		String prefix = TSHelper.getPrefix(contents, position);
		ContentProposalCollector collector = new ContentProposalCollector(prefix);
		try {
			tsFile.completions(position, collector);
		} catch (TSException e) {
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
