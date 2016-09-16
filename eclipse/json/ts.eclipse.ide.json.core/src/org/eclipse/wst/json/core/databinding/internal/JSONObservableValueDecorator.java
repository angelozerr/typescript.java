package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.databinding.IJSONObservable;

public class JSONObservableValueDecorator extends DecoratingObservableValue implements IJSONObservable {

	private IJSONPath path;

	public JSONObservableValueDecorator(IObservableValue decorated, IJSONPath path) {
		super(decorated, false);
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
