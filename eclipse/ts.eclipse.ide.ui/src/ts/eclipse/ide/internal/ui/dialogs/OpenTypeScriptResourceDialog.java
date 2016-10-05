/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.ui.dialogs;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class OpenTypeScriptResourceDialog extends OpenResourceDialog {

	private final Collection<IResource> existingFiles;

	public OpenTypeScriptResourceDialog(Shell shell, boolean multi, IContainer container,
			Collection<IResource> existingFiles, int typesMask) {
		super(shell, multi, container, typesMask);
		this.existingFiles = existingFiles;
	}

	protected ItemsFilter createFilter() {
		return new TypeScriptResourceFilter();
	}

	private class TypeScriptResourceFilter extends ResourceFilter {

		@Override
		public boolean matchItem(Object item) {
			if (!super.matchItem(item)) {
				return false;
			}
			if (!TypeScriptResourceUtil.isTsOrTsxFile(item)) {
				return false;
			}
			return existingFiles != null ? !existingFiles.contains(item) : true;
		}
	}

}
