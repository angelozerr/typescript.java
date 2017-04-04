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
package ts.eclipse.ide.terminal.interpreter.internal.view;

import java.io.UnsupportedEncodingException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterListener;

/**
 * Simple View which display Terminal trace from the terminal local interpreter.
 *
 */
public class CommandTerminalDebugView extends ViewPart implements ICommandInterpreterListener {

	private Text terminalText;
	private Text terminalANSI;
	private CTabFolder folder;

	@Override
	public void createPartControl(Composite parent) {
		Composite p = new Composite(parent, SWT.NONE);
		p.setLayout(new GridLayout());
		p.setLayoutData(new GridData(GridData.FILL_BOTH));

		folder = new CTabFolder(p, SWT.NONE);
		folder.setFont(parent.getFont());
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Text tab
		terminalText = addTab(folder, "Text");
		// ANSI tab
		terminalANSI = addTab(folder, "ANSI");

	}

	public Text addTab(CTabFolder folder, String tabLabel) {
		CTabItem tabText = new CTabItem(folder, SWT.NONE);
		tabText.setText(tabLabel);

		Text terminalText = new Text(folder, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		terminalText.setLayoutData(new GridData(GridData.FILL_BOTH));
		terminalText.setFont(folder.getFont());
		tabText.setControl(terminalText);
		return terminalText;
	}

	@Override
	public void setFocus() {
		folder.setFocus();
		selectTextTabIfNeeded();
	}

	private class ClearAction extends Action {

		public ClearAction() {
			super("Clear",
					PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		}

		@Override
		public void run() {
			terminalText.setText("");
			terminalANSI.setText("");
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		addActions();
		CommandTerminalService.getInstance().addCommandTerminalListener(this);
	}

	private void addActions() {
		Action action = new ClearAction();
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(action);
	}

	@Override
	public void dispose() {
		super.dispose();
		CommandTerminalService.getInstance().removeCommandTerminalListener(this);
	}

	@Override
	public void onOpenTerminal(String initialWorkingDir, String initialCommand, String userHome) {
		StringBuilder code = new StringBuilder("TrackerTest test = new TrackerTest(");
		if (initialWorkingDir == null) {
			code.append("null");
		} else {
			code.append("\"");
			code.append(initialWorkingDir.replaceAll("[\"]", "\\\"").replaceAll("\\\\", "\\\\\\\\"));
			code.append("\"");
		}
		code.append(", ");
		if (initialCommand == null) {
			code.append("null");
		} else {
			code.append("\"");
			code.append(initialCommand.replaceAll("[\"]", "\\\"").replaceAll("\\\\", "\\\\\\\\"));
			code.append("\"");
		}
		code.append(", ");
		code.append("\"");
		code.append(userHome);
		code.append("\"");
		code.append(");");

		appendText(code.toString(), terminalText);
	}

	@Override
	public void onProcessText(String text, int columns) {
		StringBuilder code = new StringBuilder("test.processText(");
		code.append("\"");
		code.append(text.replaceAll("[\"]", "\\\"").replaceAll("\\\\", "\\\\\\\\"));
		code.append("\", ");
		code.append(columns);
		code.append(");");
		appendText(code.toString(), terminalText);
	}

	@Override
	public void onCarriageReturnLineFeed() {
		StringBuilder code = new StringBuilder("test.processCarriageReturnLineFeed();");
		appendText(code.toString(), terminalText);
	}

	@Override
	public void onContentReadFromStream(byte[] byteBuffer, int bytesRead, String encoding) {
		String text = getText(byteBuffer, bytesRead, encoding);
		appendText(text, terminalANSI);
	}

	private String getText(byte[] byteBuffer, int bytesRead, String encoding) {
		if (encoding == null) {
			return new String(byteBuffer, 0, bytesRead);
		}
		try {
			return new String(byteBuffer, 0, bytesRead, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(byteBuffer, 0, bytesRead);
		}
	}

	private void appendText(String s, Text text) {
		text.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				selectTextTabIfNeeded();
				if (text.getText().length() > 0) {
					text.append(System.getProperty("line.separator"));
				}
				text.append(s);
			}
		});
	}

	private void selectTextTabIfNeeded() {
		if (folder.getSelectionIndex() == -1) {
			folder.setSelection(0);
		}
	}
}
