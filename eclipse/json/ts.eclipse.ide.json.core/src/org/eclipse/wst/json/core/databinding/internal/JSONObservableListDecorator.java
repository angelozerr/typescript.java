package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.databinding.IJSONObservable;

public class JSONObservableListDecorator extends DecoratingObservableList implements IJSONObservable {
	private IJSONPath path;

	/**
	 * @param decorated
	 * @param path
	 */
	public JSONObservableListDecorator(IObservableList decorated, IJSONPath path) {
		super(decorated, true);
		this.path = path;
	}

	@Override
	public synchronized void dispose() {
		this.path = null;
		super.dispose();
	}

	@Override
	public Object getObserved() {
		IObservable decorated = getDecorated();
		if (decorated instanceof IObserving)
			return ((IObserving) decorated).getObserved();
		return null;
	}

	@Override
	public IJSONPath getPath() {
		return path;
	}
}
