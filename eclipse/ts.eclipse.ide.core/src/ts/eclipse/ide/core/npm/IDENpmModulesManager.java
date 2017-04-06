package ts.eclipse.ide.core.npm;

import ts.eclipse.ide.core.utils.OSHelper;
import ts.npm.NpmModulesManager;

public class IDENpmModulesManager extends NpmModulesManager {

	private static final IDENpmModulesManager INSTANCE = new IDENpmModulesManager();

	private IDENpmModulesManager() {
		super(OSHelper.getOs());
	}

	public static NpmModulesManager getInstance() {
		return INSTANCE;
	}

}
