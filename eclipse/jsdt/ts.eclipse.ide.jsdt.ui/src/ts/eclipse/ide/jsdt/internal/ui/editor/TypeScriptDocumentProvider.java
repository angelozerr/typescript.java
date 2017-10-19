/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - added save actions
 */
package ts.eclipse.ide.jsdt.internal.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.JavaDocumentSetupParticipant;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;

import ts.client.CodeEdit;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;
import ts.eclipse.jface.text.DocumentUtils;

public class TypeScriptDocumentProvider extends TextFileDocumentProvider {

	public TypeScriptDocumentProvider() {
		IDocumentProvider provider = new TextFileDocumentProvider();
		provider = new ForwardingDocumentProvider(IJavaScriptPartitions.JAVA_PARTITIONING,
				new JavaDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);
	}

	@Override
	protected DocumentProviderOperation createSaveOperation(Object element, IDocument document, boolean overwrite)
			throws CoreException {
		final DocumentProviderOperation delegate = super.createSaveOperation(element, document, overwrite);
		return new DocumentProviderOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException {
				SubMonitor progress = SubMonitor.convert(monitor, 10);

				// Retrieve the file that is being saved
				IFile file = getFile(element);
				if (file == null) {
					return;
				}
				IPreferenceStore preferenceStore = createProjectSpecificPreferenceStore(file.getProject());
				boolean runSaveActions = preferenceStore
						.getBoolean(TypeScriptUIPreferenceConstants.EDITOR_SAVE_ACTIONS);

				try {
					delegate.run(progress.newChild(8));
					if (runSaveActions) {
						try {
							performSaveActions(file, document, progress.newChild(2), preferenceStore);
						} catch (Exception e) {
							JSDTTypeScriptUIPlugin.log(e);
						}
					} else {
						progress.setWorkRemaining(0);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (InvocationTargetException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID, "Error while saving " + file, e));
				}
			}
		};
	}

	private void performSaveActions(IFile file, IDocument document, IProgressMonitor monitor,
			IPreferenceStore preferenceStore) {
		boolean runFormat = preferenceStore.getBoolean(TypeScriptUIPreferenceConstants.EDITOR_SAVE_ACTIONS_FORMAT);
		SubMonitor progress = SubMonitor.convert(monitor, (runFormat ? 10 : 0));
		if (!runFormat) {
			return;
		}

		IUndoManager manager = RefactoringCore.getUndoManager();

		CompositeChange saveActionsChange = new CompositeChange("Save Actions");
		List<Change> undoChanges = new ArrayList<>();
		boolean success = false;
		try {
			manager.aboutToPerformChange(saveActionsChange);

			// Format the file contents
			if (runFormat) {
				TextFileChange change = new TextFileChange("Format", file);
				try {
					IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(file.getProject());
					final IIDETypeScriptFile tsFile = tsProject.openFile(file, document);
					List<CodeEdit> codeEdits = tsFile.format(0, document.getLength()).get();
					change.setEdit(DocumentUtils.toTextEdit(codeEdits, document));
					change.initializeValidationData(new NullProgressMonitor());
					PerformChangeOperation performChangeOperation = new PerformChangeOperation(change);
					ResourcesPlugin.getWorkspace().run(performChangeOperation, progress.newChild(10));
					Change undoChange = performChangeOperation.getUndoChange();
					if (undoChange != null) {
						undoChanges.add(undoChange);
					}
				} catch (Exception e) {
					JSDTTypeScriptUIPlugin.log(e);
				}
			}

			success = true;
		} finally {
			manager.changePerformed(saveActionsChange, success);
		}

		// Add an undo change if possible
		if (!undoChanges.isEmpty()) {
			manager.addUndo(saveActionsChange.getName(), new CompositeChange(saveActionsChange.getName(),
					undoChanges.toArray(new Change[undoChanges.size()])));
		}
	}

	private static IPreferenceStore createProjectSpecificPreferenceStore(IProject project) {
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>();
		if (project != null) {
			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project), TypeScriptUIPlugin.PLUGIN_ID));
			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project), TypeScriptCorePlugin.PLUGIN_ID));
		}
		stores.add(new ScopedPreferenceStore(InstanceScope.INSTANCE, TypeScriptUIPlugin.PLUGIN_ID));
		stores.add(new ScopedPreferenceStore(InstanceScope.INSTANCE, TypeScriptCorePlugin.PLUGIN_ID));
		stores.add(new ScopedPreferenceStore(DefaultScope.INSTANCE, TypeScriptUIPlugin.PLUGIN_ID));
		stores.add(new ScopedPreferenceStore(DefaultScope.INSTANCE, TypeScriptCorePlugin.PLUGIN_ID));
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}

	private IFile getFile(Object element) {
		return getFile(getFileInfo(element));
	}

	private IFile getFile(FileInfo fileInfo) {
		if (fileInfo != null && fileInfo.fElement instanceof IFileEditorInput) {
			return ((IFileEditorInput) fileInfo.fElement).getFile();
		}
		return null;
	}
}
