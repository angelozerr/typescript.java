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
package ts.eclipse.ide.internal.ui.perspectives;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;

/**
 * Angular2 Perspective.
 *
 */
public class TypeScriptPerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		left.addPlaceholder(IPageLayout.ID_RES_NAV);

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
		bottom.addView("org.eclipse.tm.terminal.view.ui.TerminalsView");
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);

		bottom.addPlaceholder(TemplatesView.ID);
		bottom.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		bottom.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		bottom.addPlaceholder(IPageLayout.ID_TASK_LIST);
		bottom.addPlaceholder(IPageLayout.ID_PROP_SHEET);

		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.75, editorArea);

	}

}
