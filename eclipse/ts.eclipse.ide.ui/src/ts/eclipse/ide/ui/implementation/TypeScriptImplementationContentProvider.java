package ts.eclipse.ide.ui.implementation;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class TypeScriptImplementationContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent instanceof List<?>) {
			return ((List<?>) parent).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
