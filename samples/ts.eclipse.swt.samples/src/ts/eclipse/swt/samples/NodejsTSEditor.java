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

import ts.TSException;
import ts.eclipse.jface.fieldassist.TypeScriptContentProposalProvider;
import ts.eclipse.jface.viewers.TSLabelProvider;
import ts.eclipse.swt.SWTTextTypeScriptFile;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;
import ts.resources.TypeScriptProject;
import ts.server.ITypeScriptServiceClientFactory;
import ts.server.nodejs.NodeJSTypeScriptServiceClientFactory;

public class NodejsTSEditor {

	public static void main(String[] args) {
		NodejsTSEditor editor = new NodejsTSEditor();
		try {
			editor.createUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createUI() throws TSException, IOException, InterruptedException {

		ITypeScriptServiceClientFactory factory = new NodeJSTypeScriptServiceClientFactory(
				new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
		File projectDir = new File("./samples");
		ITypeScriptProject project = new TypeScriptProject(projectDir, factory);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setText("TypeScript SWT Eclipse");
		shell.setLayout(new GridLayout());

		final Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setEnabled(false);
		saveButton.setLayoutData(new GridData());

		// Tu cr�es ton text
		Text text = new Text(shell, SWT.MULTI | SWT.BORDER);
		text.setText("var s = \"\";s.");

		ITypeScriptFile file = new SWTTextTypeScriptFile("sample2.ts", text);
		project.openFile(file);

		char[] autoActivationCharacters = new char[] { '.' };
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				new TypeScriptContentProposalProvider(file.getName(), project), keyStroke, autoActivationCharacters);
		adapter.setLabelProvider(TSLabelProvider.getInstance());
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
		project.dispose();
		display.dispose();
	}

}
