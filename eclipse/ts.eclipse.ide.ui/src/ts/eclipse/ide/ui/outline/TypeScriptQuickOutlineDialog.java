package ts.eclipse.ide.ui.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

import ts.client.navbar.NavigationBarItemRoot;
import ts.eclipse.ide.internal.ui.text.AbstractInformationControl;
import ts.resources.INavbarListener;
import ts.resources.ITypeScriptFile;

public class TypeScriptQuickOutlineDialog extends AbstractInformationControl implements INavbarListener {

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent shell
	 * @param shellStyle
	 *            The shell style
	 * @param editor
	 *            Current ts editor
	 */
	public TypeScriptQuickOutlineDialog(Shell parent, int shellStyle, ITypeScriptFile tsFile) {
		super(parent, shellStyle, tsFile);
		tsFile.addNavbarListener(this);
	}

	@Override
	protected Object getInitialInput() {
		return tsFile.getNavBar();
	}

	@Override
	public void dispose() {
		if (tsFile != null) {
			this.tsFile.removeNavbarListener(this);
		}
		super.dispose();
	}

	@Override
	public void navBarChanged(final NavigationBarItemRoot navbar) {
		final TreeViewer treeViewer = getTreeViewer();
		if (treeViewer != null) {
			treeViewer.getTree().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					treeViewer.setInput(navbar);
				}
			});
		}
	}

	@Override
	protected ITreeContentProvider getContentProvider() {
		return new TypeScriptOutlineContentProvider();
	}

	@Override
	protected ILabelProvider getLabelProvider() {
		return new TypeScriptOutlineLabelProvider();
	}
}