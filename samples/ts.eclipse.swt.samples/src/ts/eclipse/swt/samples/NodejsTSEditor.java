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
import ts.doc.IJSDocument;
import ts.eclipse.jface.fieldassist.TSContentProposalProvider;
import ts.eclipse.jface.viewers.TSLabelProvider;
import ts.eclipse.swt.JSDocumentText;
import ts.server.ITSClient;
import ts.server.nodejs.NodeJSTSClient;

public class NodejsTSEditor {

	public static void main(String[] args) {
		NodejsTSEditor editor = new NodejsTSEditor();
		try {
			editor.createUI();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createUI() throws TSException, IOException,
			InterruptedException {

		ITSClient client = new NodeJSTSClient(new File("./samples"), new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
//		ITSProject project = TSProjectFactory.create();
//		project.addLib(TSDef.browser);
//		project.save();
//		
//		File nodejsTSBaseDir = new File("../../core/ternjs/node_modules/tern");
//		NodejsProcessManager.getInstance().init(nodejsTSBaseDir);
//
//		ITSServer server = new NodejsTSServer(project);
//		((NodejsTSServer) server).addInterceptor(LoggingInterceptor
//				.getInstance());
//		((NodejsTSServer) server)
//				.addProcessListener(PrintNodejsProcessListener.getInstance());

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
		text.setText("var a = [];\na.");
		IJSDocument document = new JSDocumentText("sample.ts", client, text);

		// Les charact�res qui d�clenchent l'autocompl�tion
		char[] autoActivationCharacters = new char[] { '.' };
		// La combinaison de touches qui d�clenche l'autocompl�tion
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(text,
				new TextContentAdapter(), new TSContentProposalProvider(
						document), keyStroke, autoActivationCharacters);
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
		client.dispose();
		display.dispose();
	}

}
