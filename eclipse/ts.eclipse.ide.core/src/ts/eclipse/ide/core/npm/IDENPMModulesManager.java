package ts.eclipse.ide.core.npm;

import ts.eclipse.ide.core.utils.OSHelper;
import ts.npm.NPMModulesManager;

public class IDENPMModulesManager extends NPMModulesManager {

	private static final IDENPMModulesManager INSTANCE = new IDENPMModulesManager();

	private IDENPMModulesManager() {
		super(OSHelper.getOs());
	}

	public static NPMModulesManager getInstance() {
		return INSTANCE;
	}

}
