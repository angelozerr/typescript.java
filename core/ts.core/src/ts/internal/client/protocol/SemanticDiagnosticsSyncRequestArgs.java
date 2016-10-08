package ts.internal.client.protocol;

public class SemanticDiagnosticsSyncRequestArgs extends FileRequestArgs {

	public SemanticDiagnosticsSyncRequestArgs(String file, Boolean includeLinePosition) {
		super(file);
		if (includeLinePosition != null) {
			super.add("includeLinePosition", includeLinePosition);
		}
	}
}
