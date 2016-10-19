package ts.client.codefixes;

import java.util.List;

public class CodeAction {

	/** Description of the code action to display in the UI of the editor */
	private String description;
	/** Text changes to apply to each file as part of the code action */
	private List<FileCodeEdits> changes;
	
	public String getDescription() {
		return description;
	}
	
	public List<FileCodeEdits> getChanges() {
		return changes;
	}
}
