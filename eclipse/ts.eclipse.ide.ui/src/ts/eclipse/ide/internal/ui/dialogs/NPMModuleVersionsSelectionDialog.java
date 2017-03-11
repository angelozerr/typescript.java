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
package ts.eclipse.ide.internal.ui.dialogs;

import java.io.IOException;
import java.text.Collator;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import ts.eclipse.ide.core.utils.OSHelper;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.npm.NPMHelper;

/**
 * Shows a list of NPM module version to the user with a text entry field for a
 * string pattern used to filter the list of versions for a given module name.
 *
 */
public class NPMModuleVersionsSelectionDialog extends FilteredItemsSelectionDialog {

	private static final String DIALOG_SETTINGS = "ts.eclipse.ide.ui.dialogs.NPMModuleVersionsSelectionDialog"; //$NON-NLS-1$

	private final String moduleName;

	public NPMModuleVersionsSelectionDialog(String moduleName, Shell shell, boolean multi) {
		super(shell, multi);
		setTitle(NLS.bind(TypeScriptUIMessages.NPMModuleVersionsSelectionDialog_title, moduleName));
		setMessage(TypeScriptUIMessages.NPMModuleVersionsSelectionDialog_message);
		setImage(TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_NPM));
		setListLabelProvider(new VersionLabelProvider());
		this.moduleName = moduleName;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new VersionFilter();
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {
		if (itemsFilter instanceof VersionFilter) {
			progressMonitor.beginTask(TypeScriptUIMessages.NPMModuleVersionsSelectionDialog_searchJob_taskName, 1);
			try {
				// execute "npm view $moduleName versions" to retrieve version
				// list of the given module name.
				NPMHelper.getVersions(moduleName, OSHelper.getOs())
						.forEach(version -> contentProvider.add(version, itemsFilter));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		progressMonitor.done();
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = TypeScriptUIPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = TypeScriptUIPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	@Override
	public int open() {
		if (getInitialPattern() == null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				ISelection selection = window.getSelectionService().getSelection();
				if (selection instanceof ITextSelection) {
					String text = ((ITextSelection) selection).getText();
					if (text != null) {
						text = text.trim();
						if (text.length() > 0) {
							setInitialPattern(text);
						}
					}
				}
			}
		}
		return super.open();
	}

	@Override
	public String getElementName(Object item) {
		return (String) item;
	}

	@Override
	protected Comparator getItemsComparator() {
		return (v1, v2) -> {
			Collator collator = Collator.getInstance();
			int comparability = collator.compare(v1, v2);
			return comparability;
		};
	}

	@Override
	protected IStatus validateItem(Object item) {
		return new Status(IStatus.OK, TypeScriptUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	protected class VersionLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_NPM);
		}
	}

	protected class VersionFilter extends ItemsFilter {

		@Override
		public boolean isConsistentItem(Object item) {
			if (!(item instanceof String)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean matchItem(Object item) {
			if (!(item instanceof String)) {
				return false;
			}
			return nameMatches((String) item);
		}

		private boolean nameMatches(String name) {
			return matches(name);
		}
	}
}
