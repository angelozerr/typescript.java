package ts.client.protocol;

public class CloseRequest extends SimpleRequest {

	public CloseRequest(String fileName) {
		super(CommandNames.Close, new FileRequestArgs(fileName), null);
	}

}