package ts.eclipse.ide.jsdt.internal.ui.editor.format;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.PlatformUI;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.DocumentUtils;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;

/**
 * Content formatter which consumes tsserver "format" command to format a
 * {@link IDocument}.
 *
 */
public class TypeScriptContentFormatter implements IContentFormatter {

	private final IResource resource;

	public TypeScriptContentFormatter(IResource resource) {
		this.resource = resource;
	}

	@Override
	public void format(IDocument document, IRegion region) {
		try {
			IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(resource.getProject());
			final IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);
			int startPosition = region.getOffset();
			int endPosition = region.getOffset() + region.getLength() - 1;
			tsFile.format(startPosition, endPosition).thenAccept(codeEdits -> {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							TextEdit textEdit = DocumentUtils.toTextEdit(codeEdits, document);
							textEdit.apply(document, TextEdit.CREATE_UNDO);
						} catch (Exception e) {
							IStatus status = new Status(IStatus.ERROR, JSDTTypeScriptUIPlugin.PLUGIN_ID, e.getMessage(),
									e);
							ErrorDialog.openError(getShell(),
									JSDTTypeScriptUIMessages.TypeScriptContentFormatter_Error_title,
									JSDTTypeScriptUIMessages.TypeScriptContentFormatter_Error_message, status);
						}
					}
				});
			});
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, JSDTTypeScriptUIPlugin.PLUGIN_ID, e.getMessage(), e);
			ErrorDialog.openError(getShell(), JSDTTypeScriptUIMessages.TypeScriptContentFormatter_Error_title,
					JSDTTypeScriptUIMessages.TypeScriptContentFormatter_Error_message, status);
		}
	}

	private Shell getShell() {
		if (PlatformUI.isWorkbenchRunning()) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} else {
			return new Shell(Display.getCurrent());
		}
	}

	@Override
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		return null;
	}

}
