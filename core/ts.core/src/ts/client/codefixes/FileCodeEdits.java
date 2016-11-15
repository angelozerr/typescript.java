package ts.client.codefixes;

import java.util.List;

import ts.client.CodeEdit;

public class FileCodeEdits {

	private String fileName;
	private List<CodeEdit> textChanges;

	public String getFileName() {
		return fileName;
	}

	public List<CodeEdit> getTextChanges() {
		return textChanges;
	}
}
