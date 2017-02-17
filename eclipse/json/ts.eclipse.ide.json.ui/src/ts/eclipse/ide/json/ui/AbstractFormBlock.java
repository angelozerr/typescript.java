package ts.eclipse.ide.json.ui;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.Section;

import ts.eclipse.ide.json.ui.FormLayoutFactory;

public abstract class AbstractFormBlock extends MasterDetailsBlock{

	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
	    Composite container = managedForm.getToolkit().createComposite(parent);
//	    container.setLayout(FormLayoutFactory.createMasterGridLayout(false, 1));
//	    container.setLayoutData(new GridData(1808));
//	    this.fSection = createMasterSection(managedForm, container);
//	    managedForm.addPart(this.fSection);
//	    Section section = this.fSection.getSection();
//	    section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
//	    section.setLayoutData(new GridData(1808));
	}

	@Override
	protected void createToolBarActions(IManagedForm arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void registerPages(DetailsPart arg0) {
		// TODO Auto-generated method stub
		
	}
	
	protected abstract Object createMasterSection(IManagedForm managedForm, Composite container);

	  public void createContent(IManagedForm managedForm) {
	    super.createContent(managedForm);
	    managedForm.getForm().getBody().setLayout(FormLayoutFactory.createFormGridLayout(false, 1));
	  }

	  public DetailsPart getDetailsPart() {
	    return this.detailsPart;
	  }

}
