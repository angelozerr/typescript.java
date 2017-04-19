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
package ts.eclipse.ide.jsdt.internal.ui.compare;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.internal.ui.editor.EclipsePreferencesAdapter;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptSourceViewerConfiguration;
import ts.eclipse.ide.jsdt.internal.ui.editor.format.TypeScriptContentFormatter;
import ts.eclipse.ide.jsdt.internal.ui.text.TypeScriptTextTools;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;

/**
 * TypeScript merge viewer used to compare ts files.
 *
 */
public class TypeScriptMergeViewer extends TextMergeViewer {

	private IPropertyChangeListener fPreferenceChangeListener;
	private IPreferenceStore fPreferenceStore;
	private Map<SourceViewer, TypeScriptSourceViewerConfiguration> fSourceViewerConfiguration;
	private Map<SourceViewer, TypeScriptEditorAdapter> fEditor;
	private ArrayList<SourceViewer> fSourceViewer;
	private IWorkbenchPartSite fSite;
	private IIDETypeScriptProject project;

	public TypeScriptMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles | SWT.LEFT_TO_RIGHT, mp);
	}

	private IPreferenceStore getPreferenceStore() {
		if (fPreferenceStore == null)
			setPreferenceStore(createChainedPreferenceStore(null));
		return fPreferenceStore;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		setPreferenceStore(null);
		super.handleDispose(event);
	}

	public IIDETypeScriptProject getTypeScriptProject(ICompareInput input) {

		if (input == null)
			return null;

		IResourceProvider rp = null;
		ITypedElement te = input.getLeft();
		if (te instanceof IResourceProvider)
			rp = (IResourceProvider) te;
		if (rp == null) {
			te = input.getRight();
			if (te instanceof IResourceProvider)
				rp = (IResourceProvider) te;
		}
		if (rp == null) {
			te = input.getAncestor();
			if (te instanceof IResourceProvider)
				rp = (IResourceProvider) te;
		}
		if (rp != null) {
			IResource resource = rp.getResource();
			if (resource != null) {
				try {
					return TypeScriptResourceUtil.getTypeScriptProject(resource.getProject());
				} catch (CoreException e) {
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof ICompareInput) {
			project = getTypeScriptProject((ICompareInput) input);
			if (project != null) {
				setPreferenceStore(createChainedPreferenceStore(project));
			}
		}
		super.setInput(input);
	}

	private ChainedPreferenceStore createChainedPreferenceStore(IIDETypeScriptProject project) {
		ArrayList<IPreferenceStore> stores = new ArrayList<>(4);
		if (project != null) {
			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project.getProject()),
					TypeScriptCorePlugin.PLUGIN_ID));
			stores.add(new EclipsePreferencesAdapter(new ProjectScope(project.getProject()),
					TypeScriptUIPlugin.PLUGIN_ID));
		}
		stores.add(JavaScriptPlugin.getDefault().getPreferenceStore());
		stores.add(new PreferencesAdapter(JavaScriptCore.getPlugin().getPluginPreferences()));
		stores.add(new PreferencesAdapter(JSDTTypeScriptUIPlugin.getDefault().getPluginPreferences()));
		stores.add(EditorsUI.getPreferenceStore());
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}

	private void handlePropertyChange(PropertyChangeEvent event) {
		if (fSourceViewerConfiguration != null) {
			for (Iterator<Entry<SourceViewer, TypeScriptSourceViewerConfiguration>> iterator = fSourceViewerConfiguration
					.entrySet().iterator(); iterator.hasNext();) {
				Entry<SourceViewer, TypeScriptSourceViewerConfiguration> entry = iterator.next();
				TypeScriptSourceViewerConfiguration configuration = entry.getValue();
				if (configuration.affectsTextPresentation(event)) {
					configuration.handlePropertyChangeEvent(event);
					ITextViewer viewer = entry.getKey();
					viewer.invalidateTextPresentation();
				}
			}
		}
	}

	@Override
	protected void configureTextViewer(TextViewer viewer) {
		if (viewer instanceof SourceViewer) {
			SourceViewer sourceViewer = (SourceViewer) viewer;
			if (fSourceViewer == null)
				fSourceViewer = new ArrayList<>();
			if (!fSourceViewer.contains(sourceViewer))
				fSourceViewer.add(sourceViewer);
			TypeScriptTextTools tools = JSDTTypeScriptUIPlugin.getDefault().getJavaTextTools();
			if (tools != null) {
				IEditorInput editorInput = getEditorInput(sourceViewer);
				sourceViewer.unconfigure();
				if (editorInput == null) {
					sourceViewer.configure(getSourceViewerConfiguration(sourceViewer, null));
					return;
				}
				getSourceViewerConfiguration(sourceViewer, editorInput);
			}
		}
	}

	/*
	 * @see
	 * org.eclipse.compare.contentmergeviewer.TextMergeViewer#setEditable(org.
	 * eclipse.jface.text.source.ISourceViewer, boolean)
	 * 
	 * @since 3.5
	 */
	@Override
	protected void setEditable(ISourceViewer sourceViewer, boolean state) {
		super.setEditable(sourceViewer, state);
		if (fEditor != null) {
			Object editor = fEditor.get(sourceViewer);
			if (editor instanceof TypeScriptEditorAdapter)
				((TypeScriptEditorAdapter) editor).setEditable(state);
		}
	}

	/*
	 * @see
	 * org.eclipse.compare.contentmergeviewer.TextMergeViewer#isEditorBacked(org
	 * .eclipse.jface.text.ITextViewer)
	 * 
	 * @since 3.5
	 */
	@Override
	protected boolean isEditorBacked(ITextViewer textViewer) {
		return getSite() != null;
	}

	@Override
	protected IEditorInput getEditorInput(ISourceViewer sourceViewer) {
		IEditorInput editorInput = super.getEditorInput(sourceViewer);
		if (editorInput == null)
			return null;
		if (getSite() == null)
			return null;
		if (!(editorInput instanceof IStorageEditorInput))
			return null;
		return editorInput;
	}

	private IWorkbenchPartSite getSite() {
		if (fSite == null) {
			IWorkbenchPart workbenchPart = getCompareConfiguration().getContainer().getWorkbenchPart();
			fSite = workbenchPart != null ? workbenchPart.getSite() : null;
		}
		return fSite;
	}

	private TypeScriptSourceViewerConfiguration getSourceViewerConfiguration(SourceViewer sourceViewer,
			IEditorInput editorInput) {
		if (fSourceViewerConfiguration == null) {
			fSourceViewerConfiguration = new HashMap<>(3);
		}
		if (fPreferenceStore == null)
			getPreferenceStore();
		TypeScriptTextTools tools = JSDTTypeScriptUIPlugin.getDefault().getJavaTextTools();
		TypeScriptSourceViewerConfiguration configuration = new TypeScriptSourceViewerConfiguration(
				tools.getColorManager(), fPreferenceStore, null, getDocumentPartitioning()) {
			@Override
			public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
				return new TypeScriptContentFormatter(project.getProject());
			}
		};
		if (editorInput != null) {
			// when input available, use editor
			TypeScriptEditorAdapter editor = fEditor.get(sourceViewer);
			try {
				editor.init((IEditorSite) editor.getSite(), editorInput);
				editor.createActions();
				configuration = new TypeScriptSourceViewerConfiguration(tools.getColorManager(), fPreferenceStore,
						editor, getDocumentPartitioning());
			} catch (PartInitException e) {
				JSDTTypeScriptUIPlugin.log(e);
			}
		}
		fSourceViewerConfiguration.put(sourceViewer, configuration);
		return fSourceViewerConfiguration.get(sourceViewer);
	}

	private void setPreferenceStore(IPreferenceStore ps) {
		if (fPreferenceChangeListener != null) {
			if (fPreferenceStore != null)
				fPreferenceStore.removePropertyChangeListener(fPreferenceChangeListener);
			fPreferenceChangeListener = null;
		}
		fPreferenceStore = ps;
		if (fPreferenceStore != null) {
			fPreferenceChangeListener = new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					handlePropertyChange(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPreferenceChangeListener);
		}
	}

	/*
	 * @see
	 * org.eclipse.compare.contentmergeviewer.TextMergeViewer#createSourceViewer
	 * (org.eclipse.swt.widgets.Composite, int)
	 * 
	 * @since 3.5
	 */
	@Override
	protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
		SourceViewer sourceViewer;
		if (getSite() != null) {
			TypeScriptEditorAdapter editor = new TypeScriptEditorAdapter(textOrientation);
			editor.createPartControl(parent);

			ISourceViewer iSourceViewer = editor.getViewer();
			Assert.isTrue(iSourceViewer instanceof SourceViewer);
			sourceViewer = (SourceViewer) iSourceViewer;
			if (fEditor == null)
				fEditor = new HashMap<>(3);
			fEditor.put(sourceViewer, editor);
		} else
			sourceViewer = super.createSourceViewer(parent, textOrientation);

		if (fSourceViewer == null)
			fSourceViewer = new ArrayList<>();
		fSourceViewer.add(sourceViewer);

		return sourceViewer;
	}

	@Override
	protected void setActionsActivated(SourceViewer sourceViewer, boolean state) {
		if (fEditor != null) {
			Object editor = fEditor.get(sourceViewer);
			if (editor instanceof TypeScriptEditorAdapter) {
				TypeScriptEditorAdapter cuea = (TypeScriptEditorAdapter) editor;
				cuea.setActionsActivated(state);

				IAction saveAction = cuea.getAction(ITextEditorActionConstants.SAVE);
				if (saveAction instanceof IPageListener) {
					PartEventAction partEventAction = (PartEventAction) saveAction;
					IWorkbenchPart compareEditorPart = getCompareConfiguration().getContainer().getWorkbenchPart();
					if (state)
						partEventAction.partActivated(compareEditorPart);
					else
						partEventAction.partDeactivated(compareEditorPart);
				}
			}
		}
	}

	@Override
	protected void createControls(Composite composite) {
		super.createControls(composite);
		IWorkbenchPart workbenchPart = getCompareConfiguration().getContainer().getWorkbenchPart();
		if (workbenchPart != null) {
			IContextService service = workbenchPart.getSite().getService(IContextService.class);
			if (service != null) {
				service.activateContext("ts.eclipse.ide.jsdt.ui.typeScriptEditorScope"); //$NON-NLS-1$
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ITextEditorExtension3.class) {
			IEditorInput activeInput = (IEditorInput) super.getAdapter(IEditorInput.class);
			if (activeInput != null) {
				for (Iterator<TypeScriptEditorAdapter> iterator = fEditor.values().iterator(); iterator.hasNext();) {
					TypeScriptEditorAdapter editor = iterator.next();
					if (activeInput.equals(editor.getEditorInput()))
						return editor;
				}
			}
			return null;
		}
		return super.getAdapter(adapter);
	}

	private class TypeScriptEditorAdapter extends TypeScriptEditor {
		private boolean fInputSet = false;
		private int fTextOrientation;
		private boolean fEditable;

		TypeScriptEditorAdapter(int textOrientation) {
			super();
			fTextOrientation = textOrientation;
			// TODO: has to be set here
			setPreferenceStore(createChainedPreferenceStore(null));
		}

		private void setEditable(boolean editable) {
			fEditable = editable;
		}

		@Override
		public IWorkbenchPartSite getSite() {
			return TypeScriptMergeViewer.this.getSite();
		}

		@Override
		public void createActions() {
			if (fInputSet) {
				super.createActions();
				// to avoid handler conflicts disable extra actions
				// we're not handling by CompareHandlerService
				// getCorrectionCommands().deregisterCommands();
				// getRefactorActionGroup().dispose();
				// getGenerateActionGroup().dispose();
			}
			// else do nothing, we will create actions later, when input is
			// available
		}

		@Override
		public void createPartControl(Composite composite) {
			SourceViewer sourceViewer = (SourceViewer) createTypeScriptSourceViewer(composite, new CompositeRuler(),
					null, false, fTextOrientation | SWT.H_SCROLL | SWT.V_SCROLL, createChainedPreferenceStore(null));
			setSourceViewer(this, sourceViewer);
			createNavigationActions();
			getSelectionProvider().addSelectionChangedListener(getSelectionChangedListener());
		}

		@Override
		protected ISourceViewer createTypeScriptSourceViewer(Composite parent, IVerticalRuler verticalRuler,
				IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
			return new AdaptedSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles,
					store) {
				@Override
				protected void handleDispose() {
					super.handleDispose();

					// dispose the compilation unit adapter
					dispose();

					fEditor.remove(this);
					if (fEditor.isEmpty()) {
						fEditor = null;
						fSite = null;
					}

					fSourceViewer.remove(this);
					if (fSourceViewer.isEmpty())
						fSourceViewer = null;

				}
			};
		}

		@Override
		protected void doSetInput(IEditorInput input) throws CoreException {
			super.doSetInput(input);
			// the editor input has been explicitly set
			fInputSet = true;
		}

		// called by
		// org.eclipse.ui.texteditor.TextEditorAction.canModifyEditor()
		@Override
		public boolean isEditable() {
			return fEditable;
		}

		@Override
		public boolean isEditorInputModifiable() {
			return fEditable;
		}

		@Override
		public boolean isEditorInputReadOnly() {
			return !fEditable;
		}

		@Override
		protected boolean isWordWrapSupported() {
			return false;
		}

		@Override
		protected void setActionsActivated(boolean state) {
			super.setActionsActivated(state);
		}

		@Override
		public void close(boolean save) {
			getDocumentProvider().disconnect(getEditorInput());
		}
	}

	// no setter to private field AbstractTextEditor.fSourceViewer
	private void setSourceViewer(ITextEditor editor, SourceViewer viewer) {
		Field field = null;
		try {
			field = AbstractTextEditor.class.getDeclaredField("fSourceViewer"); //$NON-NLS-1$
		} catch (SecurityException | NoSuchFieldException ex) {
			JSDTTypeScriptUIPlugin.log(ex);
		}
		field.setAccessible(true);
		try {
			field.set(editor, viewer);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			JSDTTypeScriptUIPlugin.log(ex);
		}
	}
}
