package ts.internal.client.protocol;

import java.util.List;

import ts.client.CodeEdit;

/**
 * Format and format on key response message.
 */
public class FormatResponse {

	private List<CodeEdit> body;

	public List<CodeEdit> getBody() {
		return body;
	}
}
