package ts.eclipse.ide.json.ui.internal;

import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class FormLayoutFactory {

	// FORM BODY
	public static final int FORM_BODY_MARGIN_TOP = 12;
	public static final int FORM_BODY_MARGIN_BOTTOM = 12;
	public static final int FORM_BODY_MARGIN_LEFT = 6;
	public static final int FORM_BODY_MARGIN_RIGHT = 6;
	public static final int FORM_BODY_HORIZONTAL_SPACING = 20;
	// Should be 20; but, we minus 3 because the section automatically pads the
	// bottom margin by that amount
	public static final int FORM_BODY_VERTICAL_SPACING = 17;
	public static final int FORM_BODY_MARGIN_HEIGHT = 0;
	public static final int FORM_BODY_MARGIN_WIDTH = 0;

	// FORM PANE
	public static final int FORM_PANE_MARGIN_TOP = 0;
	public static final int FORM_PANE_MARGIN_BOTTOM = 0;
	public static final int FORM_PANE_MARGIN_LEFT = 0;
	public static final int FORM_PANE_MARGIN_RIGHT = 0;
	public static final int FORM_PANE_HORIZONTAL_SPACING = FORM_BODY_HORIZONTAL_SPACING;
	public static final int FORM_PANE_VERTICAL_SPACING = FORM_BODY_VERTICAL_SPACING;
	public static final int FORM_PANE_MARGIN_HEIGHT = 0;
	public static final int FORM_PANE_MARGIN_WIDTH = 0;

	// SECTION CLIENT
	public static final int SECTION_CLIENT_MARGIN_TOP = 5;
	public static final int SECTION_CLIENT_MARGIN_BOTTOM = 5;
	// Should be 6; but, we minus 4 because the section automatically pads the
	// left margin by that amount
	public static final int SECTION_CLIENT_MARGIN_LEFT = 2;
	// Should be 6; but, we minus 4 because the section automatically pads the
	// right margin by that amount	
	public static final int SECTION_CLIENT_MARGIN_RIGHT = 2;
	public static final int SECTION_CLIENT_HORIZONTAL_SPACING = 5;
	public static final int SECTION_CLIENT_VERTICAL_SPACING = 5;
	public static final int SECTION_CLIENT_MARGIN_HEIGHT = 0;
	public static final int SECTION_CLIENT_MARGIN_WIDTH = 0;

	public static final int SECTION_HEADER_VERTICAL_SPACING = 6;

	/**
	 * For form bodies.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 * @return
	 */
	public static TableWrapLayout createFormTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();

		layout.topMargin = FORM_BODY_MARGIN_TOP;
		layout.bottomMargin = FORM_BODY_MARGIN_BOTTOM;
		layout.leftMargin = FORM_BODY_MARGIN_LEFT;
		layout.rightMargin = FORM_BODY_MARGIN_RIGHT;

		layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
		layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}

	/**
	 * For composites used to group sections in left and right panes.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 * @return
	 */
	public static TableWrapLayout createFormPaneTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();

		layout.topMargin = FORM_PANE_MARGIN_TOP;
		layout.bottomMargin = FORM_PANE_MARGIN_BOTTOM;
		layout.leftMargin = FORM_PANE_MARGIN_LEFT;
		layout.rightMargin = FORM_PANE_MARGIN_RIGHT;

		layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
		layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}

	/**
	 * For composites set as section clients. For composites containg form text.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 * @return
	 */
	public static TableWrapLayout createSectionClientTableWrapLayout(
			boolean makeColumnsEqualWidth, int numColumns) {
		TableWrapLayout layout = new TableWrapLayout();

		layout.topMargin = SECTION_CLIENT_MARGIN_TOP;
		layout.bottomMargin = SECTION_CLIENT_MARGIN_BOTTOM;
		layout.leftMargin = SECTION_CLIENT_MARGIN_LEFT;
		layout.rightMargin = SECTION_CLIENT_MARGIN_RIGHT;

		layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
		layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}
}
