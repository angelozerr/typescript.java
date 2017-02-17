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
package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.json.core.databinding.ExtendedJSONPath;
import org.eclipse.wst.json.core.databinding.JSONProperties;

import ts.eclipse.ide.json.ui.AbstractFormPage;
import ts.eclipse.ide.json.ui.FormLayoutFactory;

/**
 * Plugins page for tsconfig.json editor.
 *
 */
public class PluginsPage extends AbstractFormPage {

	private static final String ID = "plugins";
	private PluginsBlock pluginsBlock;
	private Button pluginsRemoveButton;
	private TableViewer pluginsViewer;

	public PluginsPage(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.PluginsPage_title);
		this.pluginsBlock = new PluginsBlock();
	}

	@Override
	protected String getFormTitleText() {
		return TsconfigEditorMessages.PluginsPage_title;
	}

	@Override
	protected void createUI(IManagedForm managedForm) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
		createLeftContent(body);
		// createRightContent(body);
	}

	private void createLeftContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite left = toolkit.createComposite(parent);
		left.setLayout(FormLayoutFactory.createFormPaneGridLayout(false, 1));
		left.setLayoutData(new GridData(GridData.FILL_BOTH));
		createPluginsSection(left);
	}

	/**
	 * Create Files section.
	 * 
	 * @param parent
	 */
	private void createPluginsSection(Composite parent) {
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

		final Button pluginsAddButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_add,
				SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pluginsAddButton.setLayoutData(gd);

		pluginsRemoveButton = toolkit.createButton(buttonsComposite, TsconfigEditorMessages.Button_remove, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pluginsRemoveButton.setLayoutData(gd);
		pluginsRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// removeSelectedItems(filesViewer);
			}
		});

		// Files table
		pluginsViewer = new TableViewer(table);
		pluginsViewer.setLabelProvider(new DecoratingLabelProvider(PluginsLabelProvider.getInstance(), PluginsLabelProvider.getInstance()));
		// open file when row is double clicked
//		pluginsViewer.addDoubleClickListener(new IDoubleClickListener() {
//
//			@Override
//			public void doubleClick(DoubleClickEvent e) {
//				openFile(pluginsViewer.getSelection());
//			}
//		});
//		// update enable/disable of buttons when selection changed
//		pluginsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				updateFilesOpenButton(event.getSelection());
//				filesRemoveButton.setEnabled(!event.getSelection().isEmpty());
//			}
//		});
//		pluginsViewer.getTable().addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent e) {
//				if (e.keyCode == SWT.DEL) {
//					removeSelectedItems(pluginsViewer);
//				}
//			}
//		});
		toolkit.paintBordersFor(client);
		section.setClient(client);
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		IObservableList files = JSONProperties.list(new ExtendedJSONPath("compilerOptions.plugins[*]"))
				.observe(getEditor().getDocument());
		pluginsViewer.setContentProvider(new ObservableListContentProvider());
		pluginsViewer.setInput(files);

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

	private IFile getTsconfigFile() {
		return getEditor().getFile();
	}

}
