package ts.eclipse.swt;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ts.resources.AbstractTypeScriptFile;

public class SWTTextTypeScriptFile extends AbstractTypeScriptFile {

	private final Text text;

	public SWTTextTypeScriptFile(String name, Text text) {
		super(name);
		this.text = text;
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPrefix(int position) {
		return null;
	}
	
	@Override
	public String getContents() {
		return text.getText();
	}

}
