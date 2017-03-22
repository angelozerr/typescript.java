/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *
 */
package ts.eclipse.ide.ui.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ts.eclipse.ide.core.npm.IDENPMModulesManager;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.VersionLabelProvider;
import ts.eclipse.ide.ui.preferences.StatusInfo;
import ts.npm.NPMHelper;
import ts.npm.NPMModule;
import ts.utils.StringUtils;

/**
 * NPM install widget provides :
 * 
 * <ul>
 * <li>a text field to fill a NPM module version</li>
 * <li>a "Browse..." button to search available versions for the given node
 * module name.</li>
 * </ul>
 *
 */
public class NPMInstallWidget extends Composite {

	private final String moduleName;
	private Text versionText;
	// private Button searchButton;

	private String version;

	private final IStatusChangeListener handler;
	private ValidateVersionJob validateVersionJob;

	/**
	 * Job which loads the available version of the given npm module and
	 * validates the version field.
	 *
	 */
	private class ValidateVersionJob extends Job {

		public ValidateVersionJob() {
			super(TypeScriptUIMessages.NPMInstallWidget_ValidateVersionJob_name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				// Load the available version of the NPM module.
				NPMModule module = IDENPMModulesManager.getInstance().getNPMModule(moduleName);
				module.getAvailableVersions();
				// Validate the version field.
				Display display = NPMInstallWidget.this.getDisplay();
				if ((display != null) && (!display.isDisposed())) {
					display.asyncExec(new Runnable() {
						public void run() {
							validateVersionSynch(module);
						}
					});
				}
			} catch (Throwable e) {
				NPMInstallWidget.this.statusChanged(new StatusInfo(IStatus.ERROR, e.getMessage()));
			}
			return Status.OK_STATUS;
		}
	}

	private class VersionContentProposalProvider implements IContentProposalProvider {

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			NPMModule module = IDENPMModulesManager.getInstance().getNPMModule(moduleName);
			if (module.isLoaded()) {
				List<IContentProposal> list = new ArrayList<>();
				try {
					for (String proposal : module.getAvailableVersions()) {
						if (proposal.length() >= contents.length()
								&& proposal.substring(0, contents.length()).equalsIgnoreCase(contents)) {
							list.add(new ContentProposal(proposal));
						}
					}
				} catch (IOException e) {
					// Should never occurred.
				}
				return list.toArray(new IContentProposal[list.size()]);
			}
			return null;
		}
	}

	public NPMInstallWidget(String moduleName, IStatusChangeListener handler, Composite parent, int style) {
		super(parent, style);
		this.moduleName = moduleName;
		this.handler = handler;
		createUI(this);
	}

	private void createUI(Composite parent) {
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		this.setLayout(gl);
		this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite body = new Composite(parent, SWT.NONE);
		gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		body.setLayout(gl);
		body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label versionLabel = new Label(body, SWT.NONE);
		versionLabel.setText(moduleName + "@");

		// Version field
		versionText = new Text(body, SWT.SINGLE | SWT.BORDER);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				NPMInstallWidget.this.version = versionText.getText();
				validateVersion(version);
			}
		});
		addContentProposal(versionText);

		// Search button
		// Remove "Browse" button since Ctrl+Space can be used to open completion. 
//		searchButton = new Button(body, SWT.PUSH);
//		searchButton.setText(TypeScriptUIMessages.Browse);
//		searchButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				NPMModuleVersionsSelectionDialog dialog = new NPMModuleVersionsSelectionDialog(moduleName,
//						searchButton.getShell(), false);
//				if (dialog.open() == Window.OK) {
//					String version = (String) dialog.getFirstResult();
//					versionText.setText(version);
//				}
//			}
//		});
	}

	private void addContentProposal(Text text) {
		char[] autoActivationCharacters = null;//new char[] { '.' };
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				new VersionContentProposalProvider(), keyStroke, autoActivationCharacters);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		adapter.setPropagateKeys(true);
		adapter.setLabelProvider(VersionLabelProvider.getInstance());

	}

	/**
	 * Validate the given version and update the status of the
	 * {@link IStatusChangeListener}.
	 * 
	 * @param version
	 */
	private void validateVersion(String version) {
		if (StringUtils.isEmpty(version)) {
			// none version, the field is valid.
			statusChanged(Status.OK_STATUS);
		} else {
			NPMModule module = IDENPMModulesManager.getInstance().getNPMModule(moduleName);
			if (module.isLoaded()) {
				validateVersionSynch(module);
			} else {
				validateVersionASynch(module);
			}
		}
	}

	private void validateVersionSynch(NPMModule module) {
		try {
			List<String> availableVersions = module.getAvailableVersions();
			String version = versionText.getText();
			if (availableVersions.contains(version)) {
				statusChanged(Status.OK_STATUS);
			} else {
				statusChanged(new StatusInfo(IStatus.ERROR, NLS
						.bind(TypeScriptUIMessages.NPMInstallWidget_InvalidVersion_status, version, module.getName())));
			}
		} catch (IOException e) {
			// Should never occurred!
		}
	}

	private void validateVersionASynch(NPMModule module) {
		statusChanged(new StatusInfo(IStatus.WARNING,
				NLS.bind(TypeScriptUIMessages.NPMInstallWidget_SearchingVersions_status, module.getName())));
		if (validateVersionJob == null) {
			validateVersionJob = new ValidateVersionJob();
		} else {
			validateVersionJob.cancel();
		}
		validateVersionJob.schedule();

	}

	private void statusChanged(IStatus status) {
		handler.statusChanged(status);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (validateVersionJob != null) {
			validateVersionJob.cancel();
			validateVersionJob = null;
		}
	}

	public void setVersion(String version) {
		versionText.setText(version);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		versionText.setEnabled(enabled);
		//searchButton.setEnabled(enabled);
	}

	/**
	 * Returns the npm install command.
	 * 
	 * @return the npm install command.
	 */
	public String getNpmInstallCommand() {
		return NPMHelper.getNpmInstallCommand(moduleName, version);
	}
}
