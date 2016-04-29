package ts.eclipse.ide.internal.ui.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class BuildpathIndicatorLabelDecorator implements ILightweightLabelDecorator {

	// private class DecoratorElementChangeListener implements
	// ITypeScriptBuildPathChangedListener {
	//
	// /**
	// * {@inheritDoc}
	// */
	// public void elementChanged(ElementChangedEvent event) {
	// List<IJavaElement> changed= new ArrayList<IJavaElement>();
	// processDelta(event.getDelta(), changed);
	// if (changed.size() == 0)
	// return;
	//
	// fireChange(changed.toArray(new IJavaElement[changed.size()]));
	// }
	//
	// }

	private ListenerList fListeners;
	// private IElementChangedListener fChangeListener;

	/**
	 * {@inheritDoc}
	 */
	public void addListener(ILabelProviderListener listener) {
		// if (fChangeListener == null) {
		// fChangeListener= new DecoratorElementChangeListener();
		// TypeScriptCorePlugin.addBuildPathChangedListener(fChangeListener);
		// }

		if (fListeners == null) {
			fListeners = new ListenerList();
		}

		fListeners.add(listener);
	}

	@Override
	public void dispose() {
		/*
		 * if (fChangeListener != null) {
		 * JavaCore.removeElementChangedListener(fChangeListener);
		 * fChangeListener= null; }
		 */
		if (fListeners != null) {
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				fListeners.remove(listeners[i]);
			}
			fListeners = null;
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		if (fListeners == null)
			return;

		fListeners.remove(listener);

		// if (fListeners.isEmpty() && fChangeListener != null) {
		// JavaCore.removeElementChangedListener(fChangeListener);
		// fChangeListener= null;
		// }
	}

	/*
	 * private void fireChange(IJavaElement[] elements) { if (fListeners != null
	 * && !fListeners.isEmpty()) { LabelProviderChangedEvent event= new
	 * LabelProviderChangedEvent(this, elements); Object[] listeners=
	 * fListeners.getListeners(); for (int i= 0; i < listeners.length; i++) {
	 * ((ILabelProviderListener) listeners[i]).labelProviderChanged(event); } }
	 * }
	 */

	@Override
	public void decorate(Object element, IDecoration decoration) {
		ImageDescriptor overlay = getOverlay(element);
		if (overlay != null) {
			decoration.addOverlay(overlay, IDecoration.TOP_RIGHT);
		}
	}

	private ImageDescriptor getOverlay(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			IProject project = resource.getProject();
			if (project != null && TypeScriptResourceUtil.isTypeScriptProject(project)) {
				try {
					IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
					if (tsProject.getTypeScriptBuildPath().isRootContainer(resource)) {
						return TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.DESC_OVR_LIBRARY);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
