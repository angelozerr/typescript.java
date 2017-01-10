/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.implementation;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ts.client.FileSpan;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;

/**
 * TypeScript implementation label provider.
 *
 */
public class TypeScriptImplementationLabelProvider extends LabelProvider {

	private static final WorkbenchLabelProvider INSTANCE = new WorkbenchLabelProvider();

	@Override
	public String getText(Object element) {
		if (element instanceof FileSpan) {
			String filename = getFilename(((FileSpan) element));
			return NLS.bind(TypeScriptUIMessages.TypeScriptImplementationLabelProvider_text, filename);
		}
		return super.getText(element);
	}

	private String getFilename(FileSpan span) {
		IFile file = getEclipseFile(span);
		return file != null ? file.getFullPath().toString() : span.getFile();
	}

	private IFile getEclipseFile(FileSpan span) {
		String filename = span.getFile();
		IFile file = WorkbenchResourceUtil.findFileFromWorkspace(filename);
		if (file != null) {
			return file;
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof FileSpan) {
			IFile file = getEclipseFile((FileSpan) element);
			if (file != null) {
				return INSTANCE.getImage(file);
			}
		}
		return super.getImage(element);
	}

}
