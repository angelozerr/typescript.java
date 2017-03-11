package ts.eclipse.ide.internal.ui.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.utils.NPMInstallWidget;

public class TypeScriptRuntimeAndNodejsWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = "TypeScriptRuntimeAndNodejsWizardPage";
	private NPMInstallWidget npmWidget;

	protected TypeScriptRuntimeAndNodejsWizardPage() {
		super(PAGE_NAME);
	}

	@Override
	protected void createBody(Composite parent) {
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		Composite composite = pageContent.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Composite controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		createNodejsBody(composite);
		createTypeScriptRuntimeBody(composite);

	}

	private void createNodejsBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.NodejsConfigurationBlock_nodejs_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		// Embedded node.js
		createEmbeddedNodejsField(group);
		// Installed node.js
		createInstalledNodejsField(group);
		// Path info.
		createNodePathInfo(group);
	}

	private void createNodePathInfo(Composite composite) {
		// TODO Auto-generated method stub

	}

	private void createInstalledNodejsField(Group group) {
		// TODO Auto-generated method stub

	}

	private void createEmbeddedNodejsField(Composite composite) {
		Combo comboBox = new Combo(composite, SWT.READ_ONLY);

		// Create combo of embedded node.js
		IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
		String[] values = new String[installs.length + 1];
		String[] valueLabels = new String[installs.length + 1];
		values[0] = "";
		valueLabels[0] = TypeScriptUIMessages.ComboBox_none;
		int i = 1;
		for (IEmbeddedNodejs install : installs) {
			values[i] = install.getId();
			valueLabels[i] = install.getName();
			i++;
		}
		comboBox.setItems(valueLabels);
		comboBox.setFont(JFaceResources.getDialogFont());

	}

	private void createTypeScriptRuntimeBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_typescript_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		// Embedded TypeScript
		createEmbeddedTypeScriptField(group);
		// Install TypeScript
		createInstallScriptField(group);
	}

	private void createEmbeddedTypeScriptField(Composite parent) {
		Combo comboBox = new Combo(parent, SWT.READ_ONLY);

	}
	
	private void createInstallScriptField(Composite parent) {
		npmWidget = new NPMInstallWidget("typescript", parent, SWT.NONE);
		
	}

	@Override
	protected void initializeDefaultValues() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean validatePage() {
		// TODO Auto-generated method stub
		return false;
	}

}
