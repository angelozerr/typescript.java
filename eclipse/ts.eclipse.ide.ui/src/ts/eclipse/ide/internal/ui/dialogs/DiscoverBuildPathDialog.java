package ts.eclipse.ide.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ts.eclipse.ide.core.resources.jsconfig.TsconfigJsonResourcesCollector;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;

public class DiscoverBuildPathDialog extends ListSelectionDialog {

	private SearchBuildPathJob searchJob;
	private final IContainer container;
	private final List<IResource> checkedElements;

	private static final WorkbenchLabelProvider INSTANCE = new WorkbenchLabelProvider();

	public DiscoverBuildPathDialog(Shell parentShell, IContainer container) {
		super(parentShell, Collections.emptyList(), new ArrayContentProvider(), new BuildPathLableProvider(),
				TypeScriptUIMessages.DiscoverBuildPathDialog_message);
		super.setTitle(TypeScriptUIMessages.DiscoverBuildPathDialog_title);
		this.container = container;
		this.checkedElements = new ArrayList<IResource>();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control composite = super.createDialogArea(parent);
		searchJob = new SearchBuildPathJob(container);
		searchJob.schedule();
		return composite;
	}

	@Override
	public boolean close() {
		searchJob.cancel();
		return super.close();
	}

	protected void okPressed() {
		List<IResource> children = (List<IResource>) getViewer().getInput();
		checkedElements.clear();
		if (children != null) {
			for (IResource element : children) {
				if (getViewer().getChecked(element)) {
					checkedElements.add(element);
				}
			}
		}
		super.okPressed();
	}

	public List<IResource> getCheckedElements() {
		return checkedElements;
	}

	private static class BuildPathLableProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return INSTANCE.getImage(element);
		}

		@Override
		public String getText(Object element) {
			return TypeScriptResourceUtil.getBuildPathLabel((IFile) element);
		}

	}

	private class SearchBuildPathJob extends Job {

		private final IContainer container;

		public SearchBuildPathJob(IContainer container) {
			super(TypeScriptUIMessages.DiscoverBuildPathDialog_SearchBuildPathJob_name);
			this.container = container;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			try {
				final TsconfigJsonResourcesCollector collector = new TsconfigJsonResourcesCollector(true);
				container.accept(collector, IResource.NONE);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						getViewer().setInput(collector.getResources());
						if (!getInitialElementSelections().isEmpty()) {
							checkInitialSelections();
						}
					}
				});
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// monitor.beginTask(NLS.bind(
			// TernToolsUIMessages.RefreshRepositoryJob_loading, url), 1);
			// load tern repository.json with HTTP client and refresh UI
			try {
				// List<ITernModule> modules = TernRepositoryHelper
				// .loadModules(url);
				monitor.worked(1);
				// repository is loaded correctly, refresh UI
				// refreshModules(modules, null);

			} catch (Throwable e) {
				// error while loading repository.json
				// refreshModules(Collections.EMPTY_LIST, e);
			}
			monitor.done();
			return Status.OK_STATUS;
		}

	}

	private void checkInitialSelections() {
		Iterator itemsToCheck = getInitialElementSelections().iterator();

		while (itemsToCheck.hasNext()) {
			getViewer().setChecked(itemsToCheck.next(), true);
		}
	}
}
