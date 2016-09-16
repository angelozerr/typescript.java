package org.eclipse.wst.json.core.databinding;

import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.json.jsonpath.IJSONPath;

public interface IJSONObservable extends IObserving {

	IJSONPath getPath();
}
