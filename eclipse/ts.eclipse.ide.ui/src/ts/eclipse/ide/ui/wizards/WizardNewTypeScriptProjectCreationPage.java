package ts.eclipse.ide.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.osgi.service.prefs.BackingStoreException;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.wizards.TypeScriptRepositoryLabelProvider;
import ts.eclipse.ide.terminal.interpreter.LineCommand;
import ts.eclipse.ide.terminal.interpreter.TerminalCommandAdapter;
import ts.eclipse.ide.ui.utils.StatusUtil;
import ts.eclipse.ide.ui.widgets.IStatusChangeListener;
import ts.eclipse.ide.ui.widgets.NpmInstallWidget;
import ts.repository.ITypeScriptRepository;

public class WizardNewTypeScriptProjectCreationPage extends AbstractWizardNewTypeScriptProjectCreationPage {

	// TypeScript Runtime
	private boolean hasEmbeddedTsRuntime;
	private Button useEmbeddedTsRuntimeButton;
	private boolean useEmbeddedTsRuntime;
	private Combo embeddedTsRuntime;
	private NpmInstallWidget installTsRuntime;

	public WizardNewTypeScriptProjectCreationPage(String pageName, BasicNewResourceWizard wizard) {
		super(pageName, wizard);
	}

	@Override
	protected void createPageBody(Composite parent) {
		super.createPageBody(parent);
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.WizardNewTypeScriptProjectCreationPage_typescript_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		ITypeScriptRepository[] repositories = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories();
		hasEmbeddedTsRuntime = repositories.length > 0;
		if (hasEmbeddedTsRuntime) {
			// Embedded TypeScript
			createEmbeddedTypeScriptField(group, repositories);
		}
		// Install TypeScript
		createInstallScriptField(group);
	}

	private void createEmbeddedTypeScriptField(Composite parent, ITypeScriptRepository[] repositories) {
		useEmbeddedTsRuntimeButton = new Button(parent, SWT.RADIO);
		useEmbeddedTsRuntimeButton
				.setText(TypeScriptUIMessages.WizardNewTypeScriptProjectCreationPage_useEmbeddedTsRuntime_label);
		useEmbeddedTsRuntimeButton.addListener(SWT.Selection, this);
		useEmbeddedTsRuntimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTsRuntimeMode();
			}
		});

		embeddedTsRuntime = new Combo(parent, SWT.READ_ONLY);
		embeddedTsRuntime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ComboViewer viewer = new ComboViewer(embeddedTsRuntime);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new TypeScriptRepositoryLabelProvider());

		viewer.setInput(repositories);
	}

	private void createInstallScriptField(Composite parent) {
		Button useInstallTsRuntime = new Button(parent, SWT.RADIO);
		useInstallTsRuntime
				.setText(TypeScriptUIMessages.WizardNewTypeScriptProjectCreationPage_useInstallTsRuntime_label);
		useInstallTsRuntime.addListener(SWT.Selection, this);
		useInstallTsRuntime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTsRuntimeMode();
			}
		});
		installTsRuntime = new NpmInstallWidget("typescript", new IStatusChangeListener() {
			@Override
			public void statusChanged(IStatus status) {
				setPageComplete(validatePage());
			}
		}, parent, SWT.NONE);
		installTsRuntime.getVersionText().addListener(SWT.Modify, this);
	}

	@Override
	protected void initializeDefaultValues() {
		super.initializeDefaultValues();

		// Default values for TypeScript runtime
		if (hasEmbeddedTsRuntime) {
			embeddedTsRuntime.select(0);
			useEmbeddedTsRuntimeButton.setSelection(true);
		}
	}

	@Override
	protected void updateComponents(Event event) {
		super.updateComponents(event);
		Widget item = event != null ? event.item : null;
		if (item == null || item == useEmbeddedTsRuntimeButton)
			updateTsRuntimeMode();
	}

	private void updateTsRuntimeMode() {
		if (!hasEmbeddedTsRuntime) {
			return;
		}
		useEmbeddedTsRuntime = useEmbeddedTsRuntimeButton.getSelection();
		embeddedTsRuntime.setEnabled(useEmbeddedTsRuntime);
		installTsRuntime.setEnabled(!useEmbeddedTsRuntime);
	}

	@Override
	protected IStatus validatePageImpl() {
		return StatusUtil.getMoreSevere(super.validatePageImpl(), validateTypeScriptRuntime());
	}

	/** Validates the selected TypeScript Runtime. */
	private IStatus validateTypeScriptRuntime() {
		if (hasEmbeddedTsRuntime && useEmbeddedTsRuntimeButton.getSelection()) {
			return Status.OK_STATUS;
		}
		return installTsRuntime.getStatus();
	}

	@Override
	public void updateCommand(List<LineCommand> commands, final IProject project, String nodeFilePath) {
		if (!useEmbeddedTsRuntime) {
			// when TypeScript is installed when "npm install typescript"
			// command is terminated, update the project Eclispe preferences
			// to consume this installed TypeScript runtime.
			commands.add(new LineCommand(installTsRuntime.getNpmInstallCommand(), new TerminalCommandAdapter() {
				@Override
				public void onTerminateCommand(LineCommand lineCommand) {

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							IEclipsePreferences preferences = new ProjectScope(project)
									.getNode(TypeScriptCorePlugin.PLUGIN_ID);
							preferences.putBoolean(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false);
							preferences.put(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
									"${project_loc:node_modules/typescript}");
							try {
								preferences.flush();
							} catch (BackingStoreException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}));
		}
	}

}
