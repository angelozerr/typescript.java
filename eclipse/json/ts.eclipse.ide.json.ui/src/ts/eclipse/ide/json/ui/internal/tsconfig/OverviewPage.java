package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import ts.eclipse.ide.json.ui.internal.AbstractFormPage;
import ts.eclipse.ide.json.ui.internal.FormLayoutFactory;

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
	protected void createUI(IManagedForm managedForm, FormToolkit toolkit) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));

		// Left part
		Composite left = toolkit.createComposite(body);
		left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// General Information
		createGeneralInformationSection(toolkit, left);
		createOutputSection(toolkit, left);
		
		// Right part
		Composite right = toolkit.createComposite(body);
		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		createDebuggingSection(toolkit, right);

	}

	private void createGeneralInformationSection(FormToolkit toolkit, Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription(TsconfigEditorMessages.OverviewPage_GeneralInformationSection_desc);
		section.setText(TsconfigEditorMessages.OverviewPage_GeneralInformationSection_title);
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		// glayout.horizontalSpacing = 10;
		glayout.numColumns = 1;
		sbody.setLayout(glayout);

		
		Composite sectionClient = toolkit.createComposite(sbody);
		sectionClient.setLayout(new GridLayout(2, false));
		Button button = toolkit.createButton(sectionClient, "ES3", SWT.RADIO);
		button = toolkit.createButton(sectionClient, "ES6", SWT.RADIO);

		toolkit.createButton(sbody, "Compile on save", SWT.CHECK);

	}
	
	private void createDebuggingSection(FormToolkit toolkit, Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription("Debugging...");
		section.setText("Debugging");
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		// glayout.horizontalSpacing = 10;
		glayout.numColumns = 1;
		sbody.setLayout(glayout);
		
		toolkit.createButton(sbody, "Generate source maps", SWT.CHECK);
	}
	
	private void createOutputSection(FormToolkit toolkit, Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setDescription("Output...");
		section.setText("Output");
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);

		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);

		GridLayout glayout = new GridLayout();
		// glayout.horizontalSpacing = 10;
		glayout.numColumns = 1;
		sbody.setLayout(glayout);
		
		toolkit.createButton(sbody, "Keep comments in JavaScript output", SWT.CHECK);
		
		toolkit.createButton(sbody, "Generate declaration files", SWT.CHECK);
		
		toolkit.createButton(sbody, "Do not emit outputs if any erros are reported", SWT.CHECK);

	}
}
