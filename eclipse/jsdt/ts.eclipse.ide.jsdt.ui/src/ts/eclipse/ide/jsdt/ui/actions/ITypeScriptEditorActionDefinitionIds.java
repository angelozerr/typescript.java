package ts.eclipse.ide.jsdt.ui.actions;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public interface ITypeScriptEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	/**
	 * Action definition ID of the source -> indent action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.indent"</code>).
	 */
	public static final String INDENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.indent"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> format action (value
	 * <code>"ts.eclipse.ide.ui.edit.text.java.format"</code>).
	 */
	public static final String FORMAT = "ts.eclipse.ide.jsdt.ui.edit.text.java.format"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> comment action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.comment"</code>).
	 */
	public static final String COMMENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.comment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> uncomment action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.uncomment"</code>).
	 */
	public static final String UNCOMMENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.uncomment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> toggle comment action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.toggle.comment"</code>).
	 * 
	 */
	public static final String TOGGLE_COMMENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.toggle.comment"; //$NON-NLS-1$
	

	/**
	 * Action definition ID of the source -> add block comment action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.add.block.comment"</code>).
	 * 
	 */
	public static final String ADD_BLOCK_COMMENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.add.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> remove block comment action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.remove.block.comment"</code>).
	 * 
	 */
	public static final String REMOVE_BLOCK_COMMENT= "ts.eclipse.ide.jsdt.ui.edit.text.java.remove.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the search -> references in project action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.search.references.in.project"</code>
	 * ).
	 */
	public static final String SEARCH_REFERENCES_IN_PROJECT = "ts.eclipse.ide.jsdt.ui.edit.text.java.search.references.in.project"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> Show Outline action
	 * (value <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.show.outline"</code>).
	 * 
	 * 
	 */
	public static final String SHOW_OUTLINE= "ts.eclipse.ide.jsdt.ui.edit.text.java.show.outline"; //$NON-NLS-1$
	
}
