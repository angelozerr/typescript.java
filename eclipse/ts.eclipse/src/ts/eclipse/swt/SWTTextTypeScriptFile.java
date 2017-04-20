package ts.eclipse.swt;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ts.resources.AbstractTypeScriptFile;
import ts.resources.ITypeScriptProject;

public class SWTTextTypeScriptFile extends AbstractTypeScriptFile {

	private final String name;
	private final Text text;

	public SWTTextTypeScriptFile(String name, Text text, ITypeScriptProject tsProject) {
		super(tsProject, null);
		this.name = name;
		this.text = text;
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});
	}

	@Override
	public String getName() {
		return name;
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
