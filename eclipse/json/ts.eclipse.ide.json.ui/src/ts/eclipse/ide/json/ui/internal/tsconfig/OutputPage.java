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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import ts.eclipse.ide.json.ui.internal.AbstractFormPage;
import ts.eclipse.ide.json.ui.internal.FormLayoutFactory;

/**
 * Output page for tsconfig.json editor.
 *
 */
public class OutputPage extends AbstractFormPage {

	private static final String ID = "output";

	public OutputPage(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.OutputPage_title);
	}
	
	@Override
	protected boolean contributeToToolbar(IToolBarManager manager) {
		manager.add(new BuildAction((TsconfigEditor) getEditor()));
		return true;
	}

	@Override
	protected String getFormTitleText() {
		return TsconfigEditorMessages.OutputPage_title;
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
		createOutputSection(left);
	}

	private void createRightContent(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Composite right = toolkit.createComposite(parent);
		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		createDebuggingSection(right);
		createReportingSection(right);
		createJSXSection(right);
	}

	private void createOutputSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OutputPage_OutputSection_desc);
		section.setText(TsconfigEditorMessages.OutputPage_OutputSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite body = createBody(section);

		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_rootDir_label,
				new JSONPath("compilerOptions.rootDir"), false);
		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_outFile_label,
				new JSONPath("compilerOptions.outFile"), true);
		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_outDir_label,
				new JSONPath("compilerOptions.outDir"), false);

		createCheckbox(body, TsconfigEditorMessages.OutputPage_noEmit_label, new JSONPath("compilerOptions.noEmit"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_noEmitHelpers_label,
				new JSONPath("compilerOptions.noEmitHelpers"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_noEmitOnError_label,
				new JSONPath("compilerOptions.noEmitOnError"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_emitDecoratorMetadata_label,
				new JSONPath("compilerOptions.emitDecoratorMetadata"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_declaration_label,
				new JSONPath("compilerOptions.declaration"));
		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_declarationDir_label,
				new JSONPath("compilerOptions.declarationDir"), false);
		createCheckbox(body, TsconfigEditorMessages.OutputPage_emitBOM_label, new JSONPath("compilerOptions.emitBOM"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_preserveConstEnums_label,
				new JSONPath("compilerOptions.preserveConstEnums"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_removeComments_label,
				new JSONPath("compilerOptions.removeComments"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_isolatedModules_label,
				new JSONPath("compilerOptions.isolatedModules"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_stripInternal_label,
				new JSONPath("compilerOptions.stripInternal"));
	}

	private void createDebuggingSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OutputPage_DebuggingSection_desc);
		section.setText(TsconfigEditorMessages.OutputPage_DebuggingSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);
		Composite body = createBody(section);

		createCheckbox(body, TsconfigEditorMessages.OutputPage_sourceMap_label,
				new JSONPath("compilerOptions.sourceMap"));
		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_sourceRoot_label,
				new JSONPath("compilerOptions.sourceRoot"), false);
		createTextAndBrowseButton(body, TsconfigEditorMessages.OutputPage_mapRoot_label,
				new JSONPath("compilerOptions.mapRoot"), false);
		createCheckbox(body, TsconfigEditorMessages.OutputPage_inlineSourceMap_label,
				new JSONPath("compilerOptions.inlineSourceMap"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_inlineSources_label,
				new JSONPath("compilerOptions.inlineSources"));
	}

	private void createReportingSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OutputPage_ReportingSection_desc);
		section.setText(TsconfigEditorMessages.OutputPage_ReportingSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite body = createBody(section);

		createCheckbox(body, TsconfigEditorMessages.OutputPage_diagnostics_label,
				new JSONPath("compilerOptions.diagnostics"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_pretty_label, new JSONPath("compilerOptions.pretty"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_traceResolution_label,
				new JSONPath("compilerOptions.traceResolution"));
		createCheckbox(body, TsconfigEditorMessages.OutputPage_listEmittedFiles_label,
				new JSONPath("compilerOptions.listEmittedFiles"));
	}

	private void createJSXSection(Composite parent) {
		FormToolkit toolkit = super.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OutputPage_JSXSection_desc);
		section.setText(TsconfigEditorMessages.OutputPage_JSXSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite body = createBody(section);

		CCombo jsxCombo = createCombo(body, TsconfigEditorMessages.OutputPage_jsx_label,
				new JSONPath("compilerOptions.jsx"), new String[] { "", "preserve", "react" });
		Text reactNamespaceText = createText(body, TsconfigEditorMessages.OutputPage_reactNamespace_label,
				new JSONPath("compilerOptions.reactNamespace"), null, "jsxFactory");
	}
	
	private Composite createBody(Section section) {
		FormToolkit toolkit = super.getToolkit();
		Composite body = toolkit.createComposite(section);
		section.setClient(body);

		GridLayout glayout = new GridLayout();
		glayout.numColumns = 1;
		body.setLayout(glayout);
		return body;
	}

}
