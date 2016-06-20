package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.tm.terminal.connector.local.launcher.LocalLauncherDelegate;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.IPreferenceKeys;
import org.osgi.framework.Bundle;

public class LocalInterpreterLauncherDelegate extends LocalLauncherDelegate {

	@Override
	public void execute(Map<String, Object> properties, Done done) {
		ITerminalServiceOutputStreamMonitorListener[] l = new ITerminalServiceOutputStreamMonitorListener[1];
		l[0] = new CommandInterpreterProcessor(properties);
		properties.put(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS, l);

		// Initialize the local terminal working directory.
		// By default, start the local terminal in the users home directory
		String initialCwd = UIPlugin.getScopedPreferences().getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD);
		String cwd = null;
		if (initialCwd == null || IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME.equals(initialCwd)
				|| "".equals(initialCwd.trim())) { //$NON-NLS-1$
			cwd = System.getProperty("user.home"); //$NON-NLS-1$
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_HOME.equals(initialCwd)) {
			String eclipseHomeLocation = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
			if (eclipseHomeLocation != null) {
				try {
					URI uri = URIUtil.fromString(eclipseHomeLocation);
					File f = URIUtil.toFile(uri);
					cwd = f.getAbsolutePath();
				} catch (URISyntaxException ex) {
					/* ignored on purpose */ }
			}
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_WS.equals(initialCwd)) {
			Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
			if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
				if (org.eclipse.core.resources.ResourcesPlugin.getWorkspace() != null
						&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot() != null
						&& org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation() != null) {
					cwd = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation()
							.toOSString();
				}
			}
		}
		if (cwd != null && !"".equals(cwd)) { //$NON-NLS-1$
			properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, cwd);
		}
		
		super.execute(properties, done);

	}

}
