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

import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import ts.eclipse.ide.json.ui.internal.AbstractFormPage;
import ts.eclipse.ide.json.ui.internal.FormLayoutFactory;

/**
 * Overview page for tsconfig.json editor.
 *
 */
public class OverviewPage extends AbstractFormPage {

	private static final String ID = "overview";

	public OverviewPage(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.OverviewPage_title);
	}

	@Override
	protected String getFormTitleText() {
		return TsconfigEditorMessages.OverviewPage_title;
	}

	@Override
	protected void createUI(IManagedForm managedForm) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));
		createLeftContent(body);
		createRightContent(body);
	}

	private void createLeftContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite left = toolkit.createComposite(parent);
		left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// General Information
		createGeneralInformationSection(left);
		createOutputSection(left);
	}

	private void createGeneralInformationSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OverviewPage_GeneralInformationSection_desc);
		section.setText(TsconfigEditorMessages.OverviewPage_GeneralInformationSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		glayout.numColumns = 1;
		sbody.setLayout(glayout);

		// Compile/Build on save
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_compileOnSave_label, new JSONPath("compileOnSave"),
				true);
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_buildOnSave_label, new JSONPath("buildOnSave"));
		// Module
		createCombo(sbody, TsconfigEditorMessages.OverviewPage_module_label, new JSONPath("compilerOptions.module"),
				new String[] { "none", "commonjs", "amd", "umd", "system", "es6", "es2015" });
		createCombo(sbody, TsconfigEditorMessages.OverviewPage_moduleResolution_label,
				new JSONPath("compilerOptions.moduleResolution"), new String[] { "node", "classic" });
		// Others....
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_experimentalDecorators_label,
				new JSONPath("compilerOptions.experimentalDecorators"));
	}

	private void createRightContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite right = toolkit.createComposite(parent);
		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Debugging section
		createDebuggingSection(right);
		// createJSXSection(right);
	}

	private void createDebuggingSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OverviewPage_DebuggingSection_desc);
		section.setText(TsconfigEditorMessages.OverviewPage_DebuggingSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		// glayout.horizontalSpacing = 10;
		glayout.numColumns = 1;
		sbody.setLayout(glayout);

		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_sourceMap_label,
				new JSONPath("compilerOptions.sourceMap"));
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_inlineSourceMap_label,
				new JSONPath("compilerOptions.inlineSourceMap"));
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_inlineSources_label,
				new JSONPath("compilerOptions.inlineSources"));
	}

	// private void createJSXSection(Composite parent) {
	// FormToolkit toolkit = super.getToolkit();
	// Section section = toolkit.createSection(parent, Section.DESCRIPTION |
	// Section.TITLE_BAR);
	// section.setDescription(TsconfigEditorMessages.OverviewPage_JSXSection_desc);
	// section.setText(TsconfigEditorMessages.OverviewPage_JSXSection_title);
	// TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
	// section.setLayoutData(data);
	//
	// Composite sbody = toolkit.createComposite(section);
	// section.setClient(sbody);
	//
	// GridLayout glayout = new GridLayout();
	// // glayout.horizontalSpacing = 10;
	// glayout.numColumns = 1;
	// sbody.setLayout(glayout);
	//
	//// createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_jsx_label,
	//// new JSONPath("compilerOptions.jsx"));
	//// createCheckbox(sbody,
	// TsconfigEditorMessages.OverviewPage_inlineSourceMap_label,
	//// new JSONPath("compilerOptions.inlineSources"));
	//// createCheckbox(sbody,
	// TsconfigEditorMessages.OverviewPage_inlineSourceMap_label,
	//// new JSONPath("compilerOptions.inlineSources"));
	// }

	private void createOutputSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OverviewPage_OutputSection_desc);
		section.setText(TsconfigEditorMessages.OverviewPage_OutputSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		// glayout.horizontalSpacing = 10;
		glayout.numColumns = 1;
		sbody.setLayout(glayout);

		// toolkit.createButton(sbody, "Keep comments in JavaScript output",
		// SWT.CHECK);
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_noEmit_label, new JSONPath("compilerOptions.noEmit"));
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_noEmitHelpers_label,
				new JSONPath("compilerOptions.noEmitHelpers"));
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_noEmitOnError_label,
				new JSONPath("compilerOptions.noEmitOnError"));
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_emitDecoratorMetadata_label,
				new JSONPath("compilerOptions.emitDecoratorMetadata"));

		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_declaration_label,
				new JSONPath("compilerOptions.declaration"));
		createTextAndBrowseButton(sbody, TsconfigEditorMessages.OverviewPage_declarationDir_label,
				new JSONPath("compilerOptions.declarationDir"), false);
		createCheckbox(sbody, TsconfigEditorMessages.OverviewPage_emitBOM_label,
				new JSONPath("compilerOptions.emitBOM"));

		// toolkit.createButton(sbody, "Generate declaration files", SWT.CHECK);
		//
		// toolkit.createButton(sbody, "Do not emit outputs if any erros are
		// reported", SWT.CHECK);

	}

}
