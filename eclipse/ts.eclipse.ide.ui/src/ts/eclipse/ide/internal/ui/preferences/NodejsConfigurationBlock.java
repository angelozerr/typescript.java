package ts.eclipse.ide.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;

public class NodejsConfigurationBlock extends OptionsConfigurationBlock {

	private static final Key PREF_USE_NODEJS_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED);
	private static final Key PREF_NODEJS_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED);
	private static final Key PREF_NODEJS_PATH = getTypeScriptCoreKey(TypeScriptCorePreferenceConstants.NODEJS_PATH);

	private Composite fControlsComposite;
	private ControlEnableState fBlockEnableState;
	private Combo embeddedComboBox;
	private Combo installedComboBox;
	private Button useEmbedNodeJs;

	public NodejsConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		fBlockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_USE_NODEJS_EMBEDDED, PREF_NODEJS_EMBEDDED, PREF_NODEJS_PATH };
	}

	public void enablePreferenceContent(boolean enable) {
		if (fControlsComposite != null && !fControlsComposite.isDisposed()) {
			if (enable) {
				if (fBlockEnableState != null) {
					fBlockEnableState.restore();
					fBlockEnableState = null;
				}
			} else {
				if (fBlockEnableState == null) {
					fBlockEnableState = ControlEnableState.disable(fControlsComposite);
				}
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		Composite nodejsComposite = createNodeJsContent(parent);
		validateSettings(null, null, null);
		return nodejsComposite;
	}

	private Composite createNodeJsContent(Composite parent) {
		final ScrolledPageContent sc1 = new ScrolledPageContent(parent);
		Composite composite = sc1.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		fControlsComposite = new Composite(composite, SWT.NONE);
		fControlsComposite.setFont(composite.getFont());
		fControlsComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		fControlsComposite.setLayout(layout);

		int nColumns = 2;

		layout = new GridLayout();
		layout.numColumns = nColumns;

		Group group = new Group(fControlsComposite, SWT.NONE);
		group.setFont(fControlsComposite.getFont());
		group.setText(TypeScriptUIMessages.NodejsConfigurationBlock_nodejs_group_label);
		group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		group.setLayout(layout);

		// Embedded node.js
		createEmbeddedNodejsField(group);
		// Installed node.js
		createInstalledNodejsField(group);
		updateComboBoxes();
		return sc1;
	}

	private void createEmbeddedNodejsField(Composite parent) {
		// Create "Embedded node.js" checkbox
		useEmbedNodeJs = addRadioBox(parent,
				TypeScriptUIMessages.NodejsConfigurationBlock_embedded_checkbox_label, PREF_USE_NODEJS_EMBEDDED,
				new String[] { "true", "true" }, 0);
		useEmbedNodeJs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

		// Create combo of embedded node.js
		IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
		String[] values = new String[installs.length];
		String[] valueLabels = new String[installs.length];
		int i = 0;
		for (IEmbeddedNodejs install : installs) {
			values[i] = install.getId();
			valueLabels[i] = install.getName();
			i++;
		}
		embeddedComboBox = newComboControl(parent, PREF_NODEJS_EMBEDDED, values, valueLabels);
		embeddedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createInstalledNodejsField(Composite parent) {
		// Create "Installed node.js" checkbox
		Button useInstalledNodeJs = addRadioBox(parent,
				TypeScriptUIMessages.NodejsConfigurationBlock_installed_checkbox_label, PREF_USE_NODEJS_EMBEDDED,
				new String[] { "false", "false" }, 0);
		useInstalledNodeJs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

		String[] defaultPaths = IDENodejsProcessHelper.getDefaultNodejsPaths();
		installedComboBox = newComboControl(parent, PREF_NODEJS_PATH, defaultPaths, defaultPaths, false);
		installedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	private void updateComboBoxes() {
		boolean useEmbedded = useEmbedNodeJs.getSelection();
		embeddedComboBox.setEnabled(useEmbedded);
		installedComboBox.setEnabled(!useEmbedded);		
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}
		if (changedKey != null) {

		}
	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

}
