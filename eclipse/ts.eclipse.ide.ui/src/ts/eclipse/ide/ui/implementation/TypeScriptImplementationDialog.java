package ts.eclipse.ide.ui.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

import ts.TypeScriptException;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.eclipse.ide.internal.ui.text.AbstractInformationControl;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;

public class TypeScriptImplementationDialog extends AbstractInformationControl {

	private List<FileSpan> fileSpans;
	
	public TypeScriptImplementationDialog(Shell parent, int shellStyle, ITypeScriptFile tsFile) {
		super(parent, shellStyle, tsFile);
		this.fileSpans = new ArrayList<FileSpan>();
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ITextSelection) {
			ITextSelection selection = (ITextSelection) input;
			try {
				final List<FileSpan> fileSpans = new ArrayList<FileSpan>(); 
				fileSpans.clear();
				tsFile.implementation(selection.getOffset(), new ITypeScriptDefinitionCollector() {

					@Override
					public void addDefinition(String file, int startLine, int startOffset, int endLine, int endOffset)
							throws TypeScriptException {
						fileSpans.add(new FileSpan(file, startLine, startOffset, endLine, endOffset));
					}
				});
				
				final TreeViewer treeViewer = getTreeViewer();
				if (treeViewer != null) {
					treeViewer.getTree().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							System.err.println("refresh");
							treeViewer.setInput(fileSpans);
						}
					});
				}
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
