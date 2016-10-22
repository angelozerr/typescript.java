package ts.eclipse.ide.internal.ui.viewers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ViewerSorter;

import ts.resources.ITypeScriptProject;

/**
 * TypeSccript elements sorters used to set "TypeScript Resources" on the top.
 *
 */
public class TypeScriptElementSorter extends ViewerSorter {

	private static final int PROJECTS = 1;
	private static final int RESOURCEFOLDERS = 7;
	private static final int RESOURCES = 8;
	private static final int OTHERS = 51;

	@Override
	public int category(Object element) {
		if (element instanceof ITypeScriptProject) {
			return PROJECTS;
		} else if (element instanceof IContainer) {
			return RESOURCEFOLDERS;
		} else if (element instanceof IFile) {
			return RESOURCES;
		}
		return OTHERS;
	}

}
