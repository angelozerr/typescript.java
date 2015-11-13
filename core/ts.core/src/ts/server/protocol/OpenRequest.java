package ts.server.protocol;

public class OpenRequest extends Request {

	public OpenRequest(String fileName, ISequenceProvider provider) {
		super(CommandNames.Open, new FileRequestArgs(fileName), provider);
	}

}
