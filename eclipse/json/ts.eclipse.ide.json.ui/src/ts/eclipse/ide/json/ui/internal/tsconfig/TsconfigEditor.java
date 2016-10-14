/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.ui.PartInitException;

import ts.eclipse.ide.json.ui.AbstractFormEditor;

/**
 * tsconfig.json editor composed with multiple page:
 * 
 * <ul>
 * <li>Overview page.</li>
 * <li>Files page.</li>
 * <li>Output page.</li>
 * <li>Source page.</li>
 * </ul>
 *
 */
public class TsconfigEditor extends AbstractFormEditor {

	@Override
	protected void doAddPages() throws PartInitException {
		addPage(new OverviewPage(this));
		addPage(new FilesPage(this));
		addPage(new OutputPage(this));
	}
}
