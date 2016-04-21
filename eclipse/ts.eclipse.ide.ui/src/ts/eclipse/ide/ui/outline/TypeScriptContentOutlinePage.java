package ts.eclipse.ide.ui.outline;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.INavbarListener;

public class TypeScriptContentOutlinePage extends Page implements IContentOutlinePage, INavbarListener {

	// private final ITypeScriptFile tsFile;
	private CommonViewer viewer;
	private IIDETypeScriptFile tsFile;

	public TypeScriptContentOutlinePage() {
		// this.tsFile = tsFile;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		this.viewer.removePostSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.viewer.setSelection(selection);
	}

	@Override
	public Control getControl() {
		return this.viewer.getControl();
	}

	@Override
	public void setFocus() {
		getControl().setFocus();
	}

	@Override
	public void createControl(Composite parent) {
		viewer = new CommonViewer(TypeScriptUIPlugin.PLUGIN_ID + ".outline", parent, SWT.MULTI);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					// if (selection.getFirstElement() instanceof JSNode) {
					// JSNode node = (JSNode) selection.getFirstElement();
					// IFile file = getFile(node);
					// if (file != null && file.exists()) {
					// Long start = node.getStart();
					// Long end = node.getEnd();
					// EditorUtils.openInEditor(file, start != null ?
					// start.intValue() : -1,
					// start != null && end != null ? end.intValue() -
					// start.intValue() : -1, true);
					// }
					// }
				}
			}
		});
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		viewer.setInput(tsFile);

	}

	public void setInput(IIDETypeScriptFile tsFile) {
		if (this.tsFile != null) {
			this.tsFile.removeNavbarListener(this);
		}

		this.tsFile = tsFile;
		this.tsFile.addNavbarListener(this);
	}

	@Override
	public void navBarChanged(final List<NavigationBarItem> items) {
		if (viewer != null) {
			viewer.getTree().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					viewer.setInput(items);
				}
			});
		}
	}

	// private IFile getFile(JSNode node) {
	// if (node.isFile()) {
	// IProject project = tsFile.getFile().getProject();
	// try {
	// IIDETypeScriptProject ternProject =
	// TypeScriptCorePlugin.getTypeScriptProject(project);
	// return ternProject.getIDEFile(node.getFile());
	// } catch (CoreException e) {
	// Trace.trace(Trace.SEVERE, "Error while getting tern project", e);
	// }
	// }
	// return tsFile.getFile();
	// }

}
