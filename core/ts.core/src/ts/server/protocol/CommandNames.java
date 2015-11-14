package ts.server.protocol;

public enum CommandNames {

	Open("open"), Change("change"), NavBar("navbar"), Completions("completions");

	private final String name;

	private CommandNames(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
