/**
 *  Copyright (c) 2013-2015 Angelo ZERR and Genuitec LLC.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Piotr Tomiak <piotr@genuitec.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.TSException;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.resources.TypeScriptResourcesManager;

/**
 * This class is responsible for tracking currently activated editors and
 * triggering synchronization of files to Tern Server. Following rules are
 * applied:
 * <ul>
 * <li>if JS or HTML file is opened, ensureSynch is called</li>
 * <li>if JS editor looses focus, it's contents are uploaded to tern server</li>
 * <li>if HTML editor looses focus, it is removed from the server on the next
 * synchronization</li>
 * </ul>
 */
public class EditorActivationTracker extends AllInOneWorkbenchListener {

	private static final EditorActivationTracker INSTANCE = new EditorActivationTracker();

	public static EditorActivationTracker getInstance() {
		return INSTANCE;
	}

	private EditorActivationTracker() {
		initialize();
		// initialize sync if needed
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench bench = PlatformUI.getWorkbench();
				if (bench.getWorkbenchWindowCount() > 0) {
					partActivated(bench.getActiveWorkbenchWindow().getActivePage().getActivePart());
				}
			}
		});
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		openTypeScriptFile(part);
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		openTypeScriptFile(part);
	}

	private void openTypeScriptFile(IWorkbenchPart part) {
		final IFile file = getFile(part);
		if (file != null && TypeScriptResourcesManager.isTSFile(file)) {
			// Ensure that everything is synchronized when TypeScript file is
			// opened
			try {
				final IIDETypeScriptProject project = TypeScriptCorePlugin.getTypeScriptProject(file.getProject());
				if (project != null) {
					final IDocument document = getDocument(part);
					if (document != null) {
						new Job("Opening TypeScript file with tsserver...") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								monitor.beginTask("", -1); //$NON-NLS-1$
								if (project.getOpenedFile(file.getProjectRelativePath().toString()) == null) {
									try {
										project.getFile(file, document);
									} catch (TSException e) {
										return new Status(IStatus.ERROR, TypeScriptUIPlugin.PLUGIN_ID, e.getMessage(),
												e);
									}
								}
								return Status.OK_STATUS;
							}
						}.schedule();
					}
				}
			} catch (CoreException e) {

			}
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		final IFile file = getFile(part);
		if (file != null && TypeScriptResourcesManager.isTSFile(file)) {
			// Ensure that everything is synchronized when TypeScript file is
			// opened
			try {
				final IIDETypeScriptProject project = TypeScriptCorePlugin.getTypeScriptProject(file.getProject());
				if (project != null) {
					final String fileName = file.getProjectRelativePath().toString();
					new Job("Closing TypeScript file with tsserver...") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask("", -1); //$NON-NLS-1$
							try {
								project.closeFile(fileName);
							} catch (TSException e) {
								return new Status(IStatus.ERROR, TypeScriptUIPlugin.PLUGIN_ID, e.getMessage(), e);
							}
							return Status.OK_STATUS;
						}
					}.schedule();
				}
			} catch (CoreException e) {

			}
		}
	}

	private IFile getFile(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile();
			}
		}
		return null;
	}

	public static IDocument getDocument(IWorkbenchPart part) {
		final IDocument document = (IDocument) part.getAdapter(IDocument.class);
		if (document != null) {
			return document;
		}
		if (part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) part;
			return editor.getDocumentProvider().getDocument(editor.getEditorInput());
		}
		return null;
	}
}
