package ts.internal.client.protocol;

public class ReferencesRequest extends FileLocationRequest {

	public ReferencesRequest(String fileName, int line, int offset) {
		super(CommandNames.References, new FileLocationRequestArgs(fileName, line, offset));
	}

}
