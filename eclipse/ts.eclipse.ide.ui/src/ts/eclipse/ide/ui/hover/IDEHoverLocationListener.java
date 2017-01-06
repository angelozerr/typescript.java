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
package ts.eclipse.ide.ui.hover;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;

import ts.eclipse.jface.text.HoverLocationListener;

/**
 * IDE hover location listener.
 *
 */
public class IDEHoverLocationListener extends HoverLocationListener {

	private final ITypeScriptHoverInfoProvider provider;

	public IDEHoverLocationListener(BrowserInformationControl control, ITypeScriptHoverInfoProvider provider) {
		super(control);
		this.provider = provider;
	}

	// @Override
	// protected void handleTernFileLink(String loc) {
	// super.handleTernFileLink(loc);
	// IFile file = provider.getTernProject().getIDEFile(loc);
	// if (file.exists()) {
	// EditorUtils.openInEditor(file, -1, -1, true);
	// }
	// }
	//
	// @Override
	// protected void handleTernDefinitionLink(String loc) {
	// super.handleTernDefinitionLink(loc);
	//
	// ITernFile tf = provider.getFile();
	// if (tf != null) {
	// IIDETernProject ternProject = provider.getTernProject();
	// Integer pos = provider.getOffset();
	// TernDefinitionQuery query = new TernDefinitionQuery(
	// tf.getFullName(ternProject), pos);
	// try {
	// ternProject.request(query, tf, this);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	// @Override
	// public void setDefinition(String filename, Long start, Long end) {
	// IFile file = getFile(filename);
	// if (file != null && file.exists()) {
	// EditorUtils.openInEditor(
	// file,
	// start != null ? start.intValue() : -1,
	// start != null && end != null ? end.intValue()
	// - start.intValue() : -1, true);
	// }
	// }
	//
	// private IFile getFile(String filename) {
	// if (StringUtils.isEmpty(filename)) {
	// return null;
	// }
	// IIDETernProject ternProject = provider.getTernProject();
	// return ternProject.getIDEFile(filename);
	// }

}
