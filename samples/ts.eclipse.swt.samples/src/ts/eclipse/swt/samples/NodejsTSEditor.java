package ts.eclipse.swt.samples;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ts.TypeScriptException;
import ts.client.LoggingInterceptor;
import ts.eclipse.jface.fieldassist.TypeScriptContentProposalProvider;
import ts.eclipse.jface.viewers.TypeScriptLabelProvider;
import ts.eclipse.swt.SWTTextTypeScriptFile;
import ts.resources.BasicTypeScriptProjectSettings;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.resources.TypeScriptProject;
import ts.utils.FileUtils;

public class NodejsTSEditor {

	public static void main(String[] args) {
		NodejsTSEditor editor = new NodejsTSEditor();
		try {
			editor.createUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createUI() throws TypeScriptException, IOException, InterruptedException {
		File nodejsInstallPath = null;
		File tsserverFile = new File("../../core/ts.repository/node_modules/typescript/bin/tsserver");
		File projectDir = new File("./samples");
		ITypeScriptProject tsProject = new TypeScriptProject(projectDir,
				new BasicTypeScriptProjectSettings(nodejsInstallPath, tsserverFile));
		tsProject.getClient().addInterceptor(new LoggingInterceptor());

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setText("TypeScript SWT Eclipse");
		shell.setLayout(new GridLayout());

		final Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setEnabled(false);
		saveButton.setLayoutData(new GridData());

		File sampleFile = new File(projectDir, "sample.ts");

		Text text = new Text(shell, SWT.MULTI | SWT.BORDER);
		text.setText(FileUtils.getContents(sampleFile));

		ITypeScriptFile tsFile = new SWTTextTypeScriptFile(FileUtils.getPath(sampleFile), text, tsProject);
		tsFile.open();

		char[] autoActivationCharacters = new char[] { '.' };
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				new TypeScriptContentProposalProvider(tsFile.getName(), tsProject), keyStroke,
				autoActivationCharacters);
		adapter.setLabelProvider(TypeScriptLabelProvider.getInstance());
		text.setLayoutData(new GridData(GridData.FILL_BOTH));

		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// editor.setDirty(false);
			}
		});
		shell.open();
		text.setFocus();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		tsProject.dispose();
		display.dispose();
	}

}
