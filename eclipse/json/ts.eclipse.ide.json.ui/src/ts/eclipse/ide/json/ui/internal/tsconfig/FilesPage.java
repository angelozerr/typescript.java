/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.json.ui.internal.tsconfig;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.wst.json.core.databinding.ExtendedJSONPath;
import org.eclipse.wst.json.core.databinding.JSONProperties;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.json.ui.internal.AbstractFormPage;
import ts.eclipse.ide.json.ui.internal.FormLayoutFactory;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.utils.DialogUtils;
import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * Scope (files, include, exclude) page for tsconfig.json editor.
 *
 */
public class FilesPage extends AbstractFormPage {

	private static final String ID = "files";
	private TableViewer filesViewer;
	private TableViewer includeViewer;
	private TableViewer excludeViewer;

	private Button filesOpenButton;
	private Button filesRemoveButton;
	private Button includeRemoveButton;
	private Button excludeRemoveButton;
	private final FilesLabelProvider filesLabelProvider;
	private static final WorkbenchLabelProvider WORKBENCH_LABEL_PROVIDER = new WorkbenchLabelProvider();

	public FilesPage(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.FilesPage_title);
		filesLabelProvider = new FilesLabelProvider();
	}

	@Override
	protected boolean contributeToToolbar(IToolBarManager manager) {
		manager.add(new BuildAction((TsconfigEditor) getEditor()));
		return true;
	}

	private class FilesLabelProvider extends LabelProvider implements ILabelDecorator {

		@Override
		public Image getImage(Object element) {
			if (isGlobPattern(element)) {
				// glob-like file patterns
				return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_GLOB_PATTERN);
			} else if (TypeScriptResourceUtil.isTsxOrJsxFile(element)) {
				return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_JSX);
			} else if (TypeScriptResourceUtil.isTsOrTsxFile(element)) {
				return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_TS);
			}
			IResource resource = getResource(element.toString());
			if (resource != null && resource.exists()) {
				return WORKBENCH_LABEL_PROVIDER.getImage(resource);
			}
			return super.getImage(element);
		}

		private boolean isGlobPattern(Object element) {
			return element.toString().contains("*") || element.toString().contains("?");
		}

		@Override
		public Image decorateImage(Image image, Object object) {
			return null;
		}

		@Override
		public String decorateText(String label, Object object) {
			if (isGlobPattern(label)) {
				return null;
			} else if (!fileExists((String) label)) {
				return label + " (not found)";
			}
			return null;
		}
	}

	@Override
	protected String getFormTitleText() {
		return TsconfigEditorMessages.FilesPage_title;
	}

	@Override
	protected void createUI(IManagedForm managedForm) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
		createLeftContent(body);
		createRightContent(body);
		updateButtons();
	}

	private void updateButtons() {
		ISelection selection = filesViewer.getSelection();
		boolean hasSelectedFile = !selection.isEmpty();
		updateFilesOpenButton(selection);
		filesRemoveButton.setEnabled(hasSelectedFile);
		includeRemoveButton.setEnabled(!includeViewer.getSelection().isEmpty());
		excludeRemoveButton.setEnabled(!excludeViewer.getSelection().isEmpty());
	}

	private void updateFilesOpenButton(ISelection selection) {
		if (filesOpenButton != null) {
			filesOpenButton.setEnabled(
					!selection.isEmpty() && fileExists((String) ((IStructuredSelection) selection).getFirstElement()));
		}
	}

	private void createLeftContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite left = toolkit.createComposite(parent);
		left.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		createFilesSection(left);
	}

	/**
	 * Create Files section.
	 * 
	 * @param parent
	 */
	private void createFilesSection(Composite parent) {
		final IFile tsconfigFile = getTsconfigFile();
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.FilesPage_FilesSection_desc);
		section.setText(TsconfigEditorMessages.FilesPage_FilesSection_title);

		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		Table table = toolkit.createTable(client, SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 100;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		// Buttons
		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		if (tsconfigFile != null) {
			final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add,
					SWT.PUSH);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			addButton.setLayoutData(gd);
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Get existing ts files
					Collection<IResource> existingFiles = getExistingFiles(tsconfigFile.getParent());
					Object[] resources = DialogUtils.openTypeScriptResourcesDialog(tsconfigFile.getProject(),
							existingFiles, addButton.getShell());
					if (resources != null && resources.length > 0) {
						IPath path = null;
						Collection<String> elements = new ArrayList<String>(resources.length);
						for (int i = 0; i < resources.length; i++) {
							path = WorkbenchResourceUtil.getRelativePath((IResource) resources[i],
									tsconfigFile.getParent());
							elements.add(path.toString());
						}
						IObservableList list = ((IObservableList) filesViewer.getInput());
						list.addAll(elements);
					}
				}

				private Collection<IResource> getExistingFiles(IContainer parent) {
					if (filesViewer.getSelection().isEmpty()) {
						return null;
					}
					Collection<IResource> resources = new ArrayList<IResource>();
					Object[] files = filesViewer.getStructuredSelection().toArray();
					for (int i = 0; i < files.length; i++) {
						IResource f = parent.getFile(new Path((String) files[i]));
						if (f.exists()) {
							resources.add(f);
						}
					}
					return resources;
				}
			});
		}

		filesRemoveButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		filesRemoveButton.setLayoutData(gd);
		filesRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedItems(filesViewer);
			}
		});

		if (tsconfigFile != null) {
			filesOpenButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_open, SWT.PUSH);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			filesOpenButton.setLayoutData(gd);
			filesOpenButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openFile(filesViewer.getSelection());
				}
			});
		}

		// Files table
		filesViewer = new TableViewer(table);
		filesViewer.setLabelProvider(new DecoratingLabelProvider(filesLabelProvider, filesLabelProvider));
		// open file when row is double clicked
		filesViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent e) {
				openFile(filesViewer.getSelection());
			}
		});
		// update enable/disable of buttons when selection changed
		filesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateFilesOpenButton(event.getSelection());
				filesRemoveButton.setEnabled(!event.getSelection().isEmpty());
			}
		});
		filesViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedItems(filesViewer);
				}
			}
		});
		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList files = JSONProperties.list(new ExtendedJSONPath("files[*]"))
				.observe(getEditor().getDocument());
		filesViewer.setContentProvider(new ObservableListContentProvider());
		filesViewer.setInput(files);

	}

	/**
	 * Remove selected files.
	 */
	private void removeSelectedItems(TableViewer viewer) {
		IObservableList list = ((IObservableList) viewer.getInput());
		list.removeAll(viewer.getStructuredSelection().toList());
	}

	/**
	 * Open in an editor the selected file of the table files.
	 * 
	 * @param selection
	 */
	private void openFile(ISelection selection) {
		if (selection.isEmpty()) {
			return;
		}
		String file = (String) ((IStructuredSelection) selection).getFirstElement();
		IFile tsconfigFile = getTsconfigFile();
		if (tsconfigFile != null) {
			IFile tsFile = tsconfigFile.getParent().getFile(new Path(file));
			if (tsFile.exists()) {
				EditorUtils.openInEditor(tsFile, true);
			}
		}
	}

	private IFile getTsconfigFile() {
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	private void createExcludeSection(Composite parent) {
		final IFile tsconfigFile = getTsconfigFile();
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.FilesPage_ExcludeSection_desc);
		section.setText(TsconfigEditorMessages.FilesPage_ExcludeSection_title);

		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		Table table = toolkit.createTable(client, SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		if (tsconfigFile != null) {
			final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add,
					SWT.PUSH);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			addButton.setLayoutData(gd);
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object[] resources = DialogUtils.openResourcesDialog(tsconfigFile.getProject(),
							addButton.getShell());
					if (resources != null && resources.length > 0) {
						IPath path = null;
						Collection<String> elements = new ArrayList<String>(resources.length);
						for (int i = 0; i < resources.length; i++) {
							path = WorkbenchResourceUtil.getRelativePath((IResource) resources[i],
									tsconfigFile.getParent());
							elements.add(path.toString());
						}
						IObservableList list = ((IObservableList) excludeViewer.getInput());
						list.addAll(elements);
					}
				}
			});
		}

		final Button addGlobButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add_pattern,
				SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addGlobButton.setLayoutData(gd);
		addGlobButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dialog = new InputDialog(addGlobButton.getShell(),
						TsconfigEditorMessages.AddPatternDialog_title, TsconfigEditorMessages.AddPatternDialog_message,
						"**/*.spec.ts", null);
				if (dialog.open() == Window.OK) {
					IObservableList list = ((IObservableList) excludeViewer.getInput());
					list.add(dialog.getValue());
				}
			}
		});

		excludeRemoveButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		excludeRemoveButton.setLayoutData(gd);
		excludeRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedItems(excludeViewer);
			}
		});

		excludeViewer = new TableViewer(table);
		excludeViewer.setLabelProvider(new DecoratingLabelProvider(filesLabelProvider, filesLabelProvider));
		// update enable/disable of buttons when selection changed
		excludeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				excludeRemoveButton.setEnabled(!event.getSelection().isEmpty());
			}
		});
		excludeViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedItems(excludeViewer);
				}
			}
		});

		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList exclude = JSONProperties.list(new ExtendedJSONPath("exclude[*]"))
				.observe(getEditor().getDocument());
		excludeViewer.setContentProvider(new ObservableListContentProvider());
		excludeViewer.setInput(exclude);

	}

	private void createIncludeSection(Composite parent) {
		final IFile tsconfigFile = getTsconfigFile();
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.FilesPage_IncludeSection_desc);
		section.setText(TsconfigEditorMessages.FilesPage_IncludeSection_title);

		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		Table table = toolkit.createTable(client, SWT.MULTI);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		if (tsconfigFile != null) {
			final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add,
					SWT.PUSH);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			addButton.setLayoutData(gd);
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object[] resources = DialogUtils.openResourcesDialog(tsconfigFile.getProject(),
							addButton.getShell());
					if (resources != null && resources.length > 0) {
						IPath path = null;
						Collection<String> elements = new ArrayList<String>(resources.length);
						for (int i = 0; i < resources.length; i++) {
							path = WorkbenchResourceUtil.getRelativePath((IResource) resources[i],
									tsconfigFile.getParent());
							elements.add(path.toString());
						}
						IObservableList list = ((IObservableList) includeViewer.getInput());
						list.addAll(elements);
					}

				}
			});
		}

		final Button addGlobButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add_pattern,
				SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addGlobButton.setLayoutData(gd);
		addGlobButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dialog = new InputDialog(addGlobButton.getShell(),
						TsconfigEditorMessages.AddPatternDialog_title, TsconfigEditorMessages.AddPatternDialog_message,
						"src/**/*", null);
				if (dialog.open() == Window.OK) {
					IObservableList list = ((IObservableList) includeViewer.getInput());
					list.add(dialog.getValue());
				}
			}
		});

		includeRemoveButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		includeRemoveButton.setLayoutData(gd);
		includeRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedItems(includeViewer);
			}
		});

		includeViewer = new TableViewer(table);
		includeViewer.setLabelProvider(new DecoratingLabelProvider(filesLabelProvider, filesLabelProvider));
		// update enable/disable of buttons when selection changed
		includeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				includeRemoveButton.setEnabled(!event.getSelection().isEmpty());
			}
		});
		includeViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedItems(includeViewer);
				}
			}
		});
		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList include = JSONProperties.list(new ExtendedJSONPath("include[*]"))
				.observe(getEditor().getDocument());
		includeViewer.setContentProvider(new ObservableListContentProvider());
		includeViewer.setInput(include);

	}

	private void createRightContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite right = toolkit.createComposite(parent);
		right.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		createExcludeSection(right);
		createIncludeSection(right);
		// createScopeSection(right);
	}

	private void createScopeSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.FilesPage_ScopeSection_desc);
		section.setText(TsconfigEditorMessages.FilesPage_ScopeSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite client = toolkit.createComposite(section);
		section.setClient(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		Table table = toolkit.createTable(client, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 100;
		table.setLayoutData(gd);
	}

	@Override
	protected void updateUIBindings() {
		super.updateUIBindings();
		excludeViewer.refresh();
		includeViewer.refresh();
		filesViewer.refresh();
		updateButtons();
	}

	public boolean fileExists(String file) {
		IFile tsconfigFile = getTsconfigFile();
		if (tsconfigFile == null) {
			return true;
		}
		return tsconfigFile.getParent().exists(new Path(file));
	}

	private IResource getResource(String file) {
		IFile tsconfigFile = getTsconfigFile();
		if (tsconfigFile == null) {
			return null;
		}
		return tsconfigFile.getParent().findMember(new Path(file));

	}

}
