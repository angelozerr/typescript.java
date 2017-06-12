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

import java.util.ArrayList;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

import ts.TypeScriptException;
import ts.client.FileSpan;
import ts.eclipse.ide.internal.ui.text.AbstractInformationControl;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;

/**
 * Popiup dialog which shows implementation for a given text selection.
 *
 */
public class TypeScriptImplementationDialog extends AbstractInformationControl {

	public TypeScriptImplementationDialog(Shell parent, int shellStyle, ITypeScriptFile tsFile) {
		super(parent, shellStyle, tsFile);
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ITextSelection) {
			ITextSelection selection = (ITextSelection) input;
			try {
				final TreeViewer treeViewer = getTreeViewer();
				tsFile.implementation(selection.getOffset()).thenAccept(spans -> {
					if (treeViewer != null && spans != null && spans.size() > 0) {
						treeViewer.getTree().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								treeViewer.setInput(spans);
							}
						});
					}
				});
			} catch (TypeScriptException e) {
				TypeScriptUIPlugin.log("Error while calling tsserver implementation command", e);
			}
		}
	}

	@Override
	protected Object getInitialInput() {
		return new ArrayList<FileSpan>();
	}

	@Override
	protected ITreeContentProvider getContentProvider() {
		return new TypeScriptImplementationContentProvider();
	}

	@Override
	protected ILabelProvider getLabelProvider() {
		return new TypeScriptImplementationLabelProvider();
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
	}
}
