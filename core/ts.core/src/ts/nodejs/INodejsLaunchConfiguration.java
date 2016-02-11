package ts.nodejs;

import java.util.List;

public interface INodejsLaunchConfiguration {

	/**
	 * Returns a list of arguments for the node command.
	 * 
	 * @return a list of arguments for the node command.
	 */
	List<String> createNodeArgs();
}
