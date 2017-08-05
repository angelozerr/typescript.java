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
package ts.eclipse.ide.ui.dialogs;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import ts.client.navto.NavtoItem;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;
import ts.utils.CompletableFutureUtils;

/**
 * Open TypeScript symbol dialog.
 *
 */
public class OpenSymbolSelectionDialog extends FilteredItemsSelectionDialog {

	private static final String DIALOG_SETTINGS = "ts.eclipse.ide.ui.dialogs.OpenSymbolSelectionDialog"; //$NON-NLS-1$
	private final ITypeScriptFile tsFile;
	private CompletableFuture<List<NavtoItem>> promise;

	public OpenSymbolSelectionDialog(ITypeScriptFile tsFile, Shell shell, boolean multi) {
		super(shell, multi);
		setTitle(TypeScriptUIMessages.OpenSymbolSelectionDialog_title);
		setMessage(TypeScriptUIMessages.OpenSymbolSelectionDialog_message);
		setImage(TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_LOGO));
		setListLabelProvider(NavtoItemLabelProvider.getInstance());
		this.tsFile = tsFile;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new SymbolFilter();
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {
		if (itemsFilter instanceof SymbolFilter) {
			progressMonitor.beginTask(TypeScriptUIMessages.OpenSymbolSelectionDialog_searchJob_taskName, 1);
			String searchValue = itemsFilter.getPattern();

			try {
				CompletableFutureUtils.cancel(promise);
				promise = tsFile.getProject().navto(tsFile.getName(), searchValue, null, false, null);
				List<NavtoItem> items = promise.get();
				for (NavtoItem item : items) {
					contentProvider.add(item, itemsFilter);
				}
				// thenAccept(items -> {
				// for (NavtoItem item : items) {
				// System.err.println(item.getName());
				// contentProvider.add(item, itemsFilter);
				// }
				// });
			} catch (Exception e) {
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
		return ((NavtoItem) item).getName();
	}

	@Override
	protected Comparator getItemsComparator() {
		return (v1, v2) -> {
			Collator collator = Collator.getInstance();
			String s1 = ((NavtoItem) v1).getName();
			String s2 = ((NavtoItem) v1).getName();
			int comparability = collator.compare(s1, s2);
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

	protected class SymbolFilter extends ItemsFilter {

		@Override
		public boolean isConsistentItem(Object item) {
			if (!(item instanceof NavtoItem)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean matchItem(Object item) {
			if (!(item instanceof NavtoItem)) {
				return false;
			}
			return nameMatches(((NavtoItem) item).getName());
		}

		private boolean nameMatches(String name) {
			return matches(name);
		}
	}
}
