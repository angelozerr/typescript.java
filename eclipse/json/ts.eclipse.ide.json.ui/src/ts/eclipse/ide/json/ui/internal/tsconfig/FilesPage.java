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

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.json.core.databinding.ExtendedJSONPath;
import org.eclipse.wst.json.core.databinding.JSONProperties;

import ts.eclipse.ide.json.ui.internal.AbstractFormPage;
import ts.eclipse.ide.json.ui.internal.FormLayoutFactory;
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

	public FilesPage(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.FilesPage_title);
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

		Table table = toolkit.createTable(client, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 100;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		// Buttons
		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addButton.setLayoutData(gd);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		Button removeButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeButton.setLayoutData(gd);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		Button openButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_open, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		openButton.setLayoutData(gd);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile(filesViewer.getSelection());
			}
		});

		// Files table
		filesViewer = new TableViewer(table);
		filesViewer.setLabelProvider(FilesLabelProvider.getInstance());
		filesViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent e) {
				openFile(filesViewer.getSelection());
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
		if (input instanceof FileEditorInput) {
			return ((FileEditorInput) input).getFile();
		}
		return null;
	}

	private void createExcludeSection(Composite parent) {
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

		Table table = toolkit.createTable(client, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addButton.setLayoutData(gd);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		Button removeButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeButton.setLayoutData(gd);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		excludeViewer = new TableViewer(table);
		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList exclude = JSONProperties.list(new ExtendedJSONPath("exclude[*]")).observe(getEditor().getDocument());
		excludeViewer.setContentProvider(new ObservableListContentProvider());
		excludeViewer.setInput(exclude);

	}

	private void createIncludeSection(Composite parent) {
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

		Table table = toolkit.createTable(client, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);

		Composite buttonsComposite = toolkit.createComposite(client);
		buttonsComposite.setLayout(new GridLayout());
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		final Button addButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addButton.setLayoutData(gd);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		Button removeButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeButton.setLayoutData(gd);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(addButton.getShell(), "TODO!", "TODO!");
			}
		});

		includeViewer = new TableViewer(table);

		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList include = JSONProperties.list(new ExtendedJSONPath("include[*]")).observe(getEditor().getDocument());
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
	}
}
