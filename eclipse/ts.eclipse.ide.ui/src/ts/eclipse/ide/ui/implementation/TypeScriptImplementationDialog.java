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
					if (treeViewer != null) {
						treeViewer.getTree().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								System.err.println("refresh");
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

}
