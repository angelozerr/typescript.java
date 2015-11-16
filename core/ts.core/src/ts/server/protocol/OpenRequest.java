package ts.server.protocol;

public class OpenRequest extends Request {

	public OpenRequest(String fileName) {
		super(CommandNames.Open, new FileRequestArgs(fileName), null);
	}

}
