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
package ts.eclipse.ide.internal.ui.preferences;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.StatusInfo;
import ts.repository.ITypeScriptRepository;
import ts.repository.TypeScriptRepositoryException;
import ts.repository.TypeScriptRepositoryManager;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * TypeScript Runtime configuration block.
 *
 */
public class TypeScriptRuntimeConfigurationBlock extends AbstractTypeScriptRepositoryConfigurationBlock {

	private static final String[] DEFAULT_PATHS = new String[] { "${project_loc:node_modules/typescript}" };

	private static final Key PREF_USE_EMBEDDED_TYPESCRIPT = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT);
	private static final Key PREF_TYPESCRIPT_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID);
	private static final Key PREF_TYPESCRIPT_PATH = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH);
	private static final Key PREF_TSSERVER_TRACE_ON_CONSOLE = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE);
	private static final Key PREF_TSSERVER_EMULATE_PLUGINS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSSERVER_EMULATE_PLUGINS);

	private Text tsRuntimePath;
	private Text tsRuntimeVersion;

	public TypeScriptRuntimeConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
	}

	@Override
	protected void createBody(Composite parent) {
		super.createBody(parent);
		super.addCheckBox(parent, TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_traceOnConsole_label,
				PREF_TSSERVER_TRACE_ON_CONSOLE, new String[] { "true", "false" }, 0);
		super.addCheckBox(parent, TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_emulatePlugins_label,
				PREF_TSSERVER_EMULATE_PLUGINS, new String[] { "true", "false" }, 0);
		createTypeScriptRuntimeInfo(parent);
	}

	private void createTypeScriptRuntimeInfo(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// TypeScript version label
		Label tsRuntimeVersionTitle = new Label(composite, SWT.NONE);
		tsRuntimeVersionTitle.setText(TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_tsRuntimeVersion_label);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		tsRuntimeVersionTitle.setLayoutData(gridData);

		tsRuntimeVersion = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		tsRuntimeVersion.setText(""); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		tsRuntimeVersion.setLayoutData(gridData);

		Label tsRuntimePathTitle = new Label(composite, SWT.NONE);
		tsRuntimePathTitle.setText(TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_tsRuntimePath_label);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		tsRuntimePathTitle.setLayoutData(gridData);

		tsRuntimePath = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		tsRuntimePath.setText(""); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		tsRuntimePath.setLayoutData(gridData);
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_USE_EMBEDDED_TYPESCRIPT, PREF_TYPESCRIPT_EMBEDDED, PREF_TYPESCRIPT_PATH,
				PREF_TSSERVER_TRACE_ON_CONSOLE, PREF_TSSERVER_EMULATE_PLUGINS };
	}

	@Override
	protected String getTypeScriptGroupLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_typescript_group_label;
	}

	@Override
	protected String getEmbeddedCheckboxLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_embedded_checkbox_label;
	}

	@Override
	protected String getInstalledCheckboxLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_installed_checkbox_label;
	}

	@Override
	protected Key getUseEmbeddedTypescriptKey() {
		return PREF_USE_EMBEDDED_TYPESCRIPT;
	}

	@Override
	protected Key getEmbeddedTypescriptKey() {
		return PREF_TYPESCRIPT_EMBEDDED;
	}

	@Override
	protected Key getInstalledTypescriptPathKey() {
		return PREF_TYPESCRIPT_PATH;
	}

	@Override
	protected String[] getDefaultPaths() {
		return DEFAULT_PATHS;
	}

	private class TypeScriptRuntimeStatus extends StatusInfo {

		private final File tsRuntimeFile;
		private final String version;

		public TypeScriptRuntimeStatus(File tsRuntimeFile, String version, String errorMessage) {
			if (errorMessage != null) {
				setError(errorMessage);
			}
			this.tsRuntimeFile = tsRuntimeFile;
			this.version = version;
		}

		public File getTsRuntimeFile() {
			return tsRuntimeFile;
		}

		public String getTsVersion() {
			return version;
		}
	}

	/**
	 * Update the TypeScript version, TypeScript labels and returns the
	 * validation status of the TypeScript path.
	 * 
	 * @return the validation status of the TypeScript path.
	 */
	private IStatus validateAndUpdateTsRuntimePath() {
		// Compute runtime status
		TypeScriptRuntimeStatus status = validateTsRuntimePath();
		// Update TypeScript version & path
		if (status.isOK()) {
			tsRuntimeVersion.setText(status.getTsVersion());
			tsRuntimePath.setText(FileUtils.getPath(status.getTsRuntimeFile()));
		} else {
			tsRuntimeVersion.setText("");
			tsRuntimePath.setText("");
		}
		return status;
	}

	/**
	 * Returns the status of the TypeScript path.
	 * 
	 * @return the status of the TypeScript path.
	 */
	private TypeScriptRuntimeStatus validateTsRuntimePath() {
		File tsRuntimeFile = null;
		String version = null;
		boolean embedded = isUseEmbedded();
		if (embedded) {
			int selectedIndex = getEmbeddedSelectionIndex();
			if (selectedIndex == 0) {
				// ERROR: the embedded TypeScript Runtime combo is not selected.
				return new TypeScriptRuntimeStatus(null, null,
						TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_embeddedTypeScript_required_error);
			} else {
				ITypeScriptRepository[] repositories = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories();
				ITypeScriptRepository repository = repositories[selectedIndex - 1];
				tsRuntimeFile = repository.getTypesScriptDir();
			}
		} else {
			String tsRuntimePath = getInstalledText();
			if (StringUtils.isEmpty(tsRuntimePath)) {
				// ERROR: the installed path is empty
				return new TypeScriptRuntimeStatus(null, null,
						TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_installedTypeScript_required_error);
			} else {
				tsRuntimeFile = WorkbenchResourceUtil.resolvePath(tsRuntimePath, getProject());
			}
		}

		if (!tsRuntimeFile.exists()) {
			// ERROR: TypeScript file doesn't exists
			return new TypeScriptRuntimeStatus(null, null,
					NLS.bind(TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_typeScriptFile_exists_error,
							FileUtils.getPath(tsRuntimeFile)));
		} else {
			try {
				TypeScriptRepositoryManager.validateTypeScriptDir(tsRuntimeFile);
			} catch (TypeScriptRepositoryException e) {
				return new TypeScriptRuntimeStatus(null, null,
						NLS.bind(TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_typeScriptFile_invalid_error,
								FileUtils.getPath(tsRuntimeFile)));
			}
			version = TypeScriptRepositoryManager.getPackageJsonVersion(tsRuntimeFile);
		}
		// Node.js path is valid
		return new TypeScriptRuntimeStatus(tsRuntimeFile, version, null);
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		IStatus status = validateAndUpdateTsRuntimePath();
		fContext.statusChanged(status);
	}
}
