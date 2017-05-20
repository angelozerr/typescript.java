package ts.client.refactors;

import java.util.List;

import ts.client.codefixes.CodeAction;

public class RefactorCodeActions {

	private List<CodeAction> actions;

	private Integer renameLocation;

	public List<CodeAction> getActions() {
		return actions;
	}

	public Integer getRenameLocation() {
		return renameLocation;
	}
}
