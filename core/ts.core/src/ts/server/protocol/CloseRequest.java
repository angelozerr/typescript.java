package ts.server.protocol;

public class CloseRequest extends Request {

	public CloseRequest(String fileName) {
		super(CommandNames.Close, new FileRequestArgs(fileName), null);
	}

}
