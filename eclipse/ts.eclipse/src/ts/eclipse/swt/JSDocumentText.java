package ts.eclipse.swt;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ts.TSException;
import ts.doc.AbstractJSDocument;
import ts.server.ITSClient;

public class JSDocumentText extends AbstractJSDocument {

	private final Text text;

	public JSDocumentText(String name, ITSClient client, Text text) {
		super(name, client, false);
		this.text = text;
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				JSDocumentText.this.setChanged(true);
			}
		});
		setChanged(false);
		try {
			client.openFile(name);
		} catch (TSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public String getValue() {
		return text.getText();
	}

}
