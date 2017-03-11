package ts.eclipse.ide.ui.utils;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.NPMModuleVersionsSelectionDialog;

public class NPMInstallWidget extends Composite {

	private final String moduleName;
	private final Text versionText;
	private Button searchButton;

	public NPMInstallWidget(String moduleName, Composite parent, int style) {
		super(parent, style);
		this.moduleName = moduleName;
		super.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.RIGHT;
		super.setLayoutData(gd);

		Label versionLabel = new Label(this, SWT.NONE);
		versionLabel.setText(moduleName + "@");
		versionText = new Text(this, SWT.SINGLE | SWT.BORDER);
		searchButton = new Button(this, SWT.PUSH);
		searchButton.setText(TypeScriptUIMessages.Browse);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NPMModuleVersionsSelectionDialog dialog = new NPMModuleVersionsSelectionDialog(moduleName,
						searchButton.getShell(), false);
				if (dialog.open() == Window.OK) {
					String version = (String) dialog.getFirstResult();
					versionText.setText(version);
				}
			}
		});
	}

}
