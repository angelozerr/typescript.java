package ts.internal.client.protocol;

import java.util.List;

import ts.client.codefixes.CodeAction;

/**
 * Code fix response.
 *
 */
public class CodeFixResponse {

	/** The code actions that are available */
	private List<CodeAction> body;

	public List<CodeAction> getBody() {
		return body;
	}

}
