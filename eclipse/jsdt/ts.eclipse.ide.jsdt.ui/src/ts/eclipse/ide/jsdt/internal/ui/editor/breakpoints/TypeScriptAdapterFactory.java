package ts.eclipse.ide.jsdt.internal.ui.editor.breakpoints;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

/**
 * Adapter factory for breakpoints
 */
public class TypeScriptAdapterFactory implements IAdapterFactory {

	static ToggleBreakpointAdapter tbadapter = null;

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IToggleBreakpointsTarget.class)) {
			return getToggleBreakpointAdapter();
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IToggleBreakpointsTarget.class };
	}

	public static synchronized ToggleBreakpointAdapter getToggleBreakpointAdapter() {
		if (tbadapter == null) {
			tbadapter = new ToggleBreakpointAdapter();
		}
		return tbadapter;
	}
	
}
