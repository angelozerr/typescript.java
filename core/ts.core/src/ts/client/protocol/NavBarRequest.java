package ts.client.protocol;

/**
 * NavBar itesm request; value of command field is "navbar". Return response
 * giving the list of navigation bar entries extracted from the requested file.
 */
public class NavBarRequest extends FileRequest {

	public NavBarRequest(String fileName) {
		super(CommandNames.NavBar.getName(), new FileRequestArgs(fileName), null);
	}

}
