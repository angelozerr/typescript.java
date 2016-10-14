/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ts.eclipse.ide.json.ui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Class from PDE
 * 
 * @see
 * https://github.com/eclipse/eclipse.pde.ui/blob/master/ui/org.eclipse.pde.ui/src/org/eclipse/pde/internal/ui/editor/FormLayoutFactory.java
 *
 */
public class FormLayoutFactory {

	// Used in place of 0. If 0 is used, widget borders will appear clipped
	// on some platforms (e.g. Windows XP Classic Theme).
	// Form tool kit requires parent composites containing the widget to have
	// at least 1 pixel border margins in order to paint the flat borders.
	// The form toolkit paints flat borders on a given widget when native
	// borders are not painted by SWT. See FormToolkit#paintBordersFor()
	public static final int DEFAULT_CLEAR_MARGIN = 2;

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

	// CLEAR
	public static final int CLEAR_MARGIN_TOP = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_BOTTOM = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_LEFT = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_MARGIN_RIGHT = DEFAULT_CLEAR_MARGIN;
	public static final int CLEAR_HORIZONTAL_SPACING = 0;
	public static final int CLEAR_VERTICAL_SPACING = 0;
	public static final int CLEAR_MARGIN_HEIGHT = 0;
	public static final int CLEAR_MARGIN_WIDTH = 0;

	/**
	 * For form bodies.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 */
	public static GridLayout createFormGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();

		layout.marginHeight = FORM_BODY_MARGIN_HEIGHT;
		layout.marginWidth = FORM_BODY_MARGIN_WIDTH;

		layout.marginTop = FORM_BODY_MARGIN_TOP;
		layout.marginBottom = FORM_BODY_MARGIN_BOTTOM;
		layout.marginLeft = FORM_BODY_MARGIN_LEFT;
		layout.marginRight = FORM_BODY_MARGIN_RIGHT;

		layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
		layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}

	/**
	 * For miscellaneous grouping composites. For sections (as a whole - header
	 * plus client).
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 */
	public static GridLayout createClearGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();

		layout.marginHeight = CLEAR_MARGIN_HEIGHT;
		layout.marginWidth = CLEAR_MARGIN_WIDTH;

		layout.marginTop = CLEAR_MARGIN_TOP;
		layout.marginBottom = CLEAR_MARGIN_BOTTOM;
		layout.marginLeft = CLEAR_MARGIN_LEFT;
		layout.marginRight = CLEAR_MARGIN_RIGHT;

		layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
		layout.verticalSpacing = CLEAR_VERTICAL_SPACING;

		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
		layout.numColumns = numColumns;

		return layout;
	}

	/**
	 * For form bodies.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 * @return
	 */
	public static TableWrapLayout createFormTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
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
	public static TableWrapLayout createFormPaneTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
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
	 * For composites used to group sections in left and right panes.
	 * 
	 * @param makeColumnsEqualWidth
	 * @param numColumns
	 */
	public static GridLayout createFormPaneGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
		GridLayout layout = new GridLayout();

		layout.marginHeight = FORM_PANE_MARGIN_HEIGHT;
		layout.marginWidth = FORM_PANE_MARGIN_WIDTH;

		layout.marginTop = FORM_PANE_MARGIN_TOP;
		layout.marginBottom = FORM_PANE_MARGIN_BOTTOM;
		layout.marginLeft = FORM_PANE_MARGIN_LEFT;
		layout.marginRight = FORM_PANE_MARGIN_RIGHT;

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
	public static TableWrapLayout createSectionClientTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
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
