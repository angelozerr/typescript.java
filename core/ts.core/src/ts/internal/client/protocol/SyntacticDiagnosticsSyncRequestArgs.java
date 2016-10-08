package ts.internal.client.protocol;

public class SyntacticDiagnosticsSyncRequestArgs extends FileRequestArgs {

	public SyntacticDiagnosticsSyncRequestArgs(String file, Boolean includeLinePosition) {
		super(file);
		if (includeLinePosition != null) {
			super.add("includeLinePosition", includeLinePosition);
		}
	}
}
