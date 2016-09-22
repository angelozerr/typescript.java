package org.eclipse.wst.json.core.databinding;

import org.eclipse.json.jsonpath.IJSONPath;

public interface IExtendedJSONPath extends IJSONPath {

	boolean isArray(int index);
}
