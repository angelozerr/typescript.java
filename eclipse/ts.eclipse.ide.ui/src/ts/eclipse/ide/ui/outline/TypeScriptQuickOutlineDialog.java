package ts.eclipse.ide.ui.outline;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.ide.StringMatcher;

import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.INavbarListener;
import ts.resources.ITypeScriptFile;

public class TypeScriptQuickOutlineDialog extends PopupDialog implements IInformationControl,
		IInformationControlExtension, IInformationControlExtension2, DisposeListener, INavbarListener {

	/**
	 * Current tree viewer for the dialog
	 */
	private TreeViewer treeViewer;

	/**
	 * Widget for filter text
	 */
	private Text filterText;

	/**
	 * @see org.eclipse.pde.internal.ui.util#StringMatcher
	 */
	private StringMatcher stringMatcher;

	/**
	 * A viewer filter is used by the dialog tree viewer to extract a subset of
	 * elements which names corresponding to a given pattern
	 */
	private QuickOutlineNamePatternFilter namePatternFilter;

	/**
	 * Content provider for the current tree viewer Instance of @see
	 * {@link TypeScriptOutlineContentProvider}
	 */
	private ITreeContentProvider treeContentProvider;

	/**
	 * Label provider for the current tree viewer Instance of @see
	 * {@link TypeScriptOutlineLabelProvider}
	 */
	private ILabelProvider treeLabelProvider;

	private ITypeScriptFile tsFile;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent shell
	 * @param shellStyle
	 *            The shell style
	 * @param editor
	 *            Current ts editor
	 */
	public TypeScriptQuickOutlineDialog(Shell parent, int shellStyle, ITypeScriptFile tsFile) {
		super(parent, shellStyle, true, true, false, true, true, null, null);
		this.tsFile = tsFile;
		this.tsFile.addNavbarListener(this);
		initialize();
		// Create all controls early to preserve the life cycle of the original
		// implementation.
		create();
	}

	/**
	 * Initialize the fields
	 */
	private void initialize() {
		setInfoText("Press 'Esc' to exit the dialog.");

		filterText = null;
		treeViewer = null;
		stringMatcher = null;
		namePatternFilter = null;
		treeContentProvider = null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// Create an empty dialog area, if the source page is not defined
		if (tsFile == null) {
			return super.createDialogArea(parent);
		}
		createTreeViewer(parent);
		createListenersTreeViewer();
		addDisposeListener(this);

		return treeViewer.getControl();
	}

	/**
	 * Gets current selected element in a tree
	 * 
	 * @return selected element
	 */
	private Object getSelectedElement() {
		if (treeViewer == null) {
			return null;
		}
		return ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
	}

	/**
	 * Creates tree viewer to manage content
	 * 
	 * @param parent
	 *            parent control
	 */
	private void createTreeViewer(Composite parent) {
		int style = SWT.H_SCROLL | SWT.V_SCROLL;
		// Create the tree
		Tree widget = new Tree(parent, style);
		// Configure the layout
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = widget.getItemHeight() * 12;
		widget.setLayoutData(data);

		treeViewer = new TreeViewer(widget);
		namePatternFilter = new QuickOutlineNamePatternFilter();
		treeViewer.addFilter(namePatternFilter);
		treeContentProvider = new TypeScriptOutlineContentProvider();
		treeViewer.setContentProvider(treeContentProvider);
		treeLabelProvider = new TypeScriptOutlineLabelProvider();
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setUseHashlookup(true);
		treeViewer.setInput(tsFile.getNavBar());
	}

	/**
	 * 
	 */
	private void createListenersTreeViewer() {
		final Tree tree = treeViewer.getTree();
		// Handle key events
		tree.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == 0x1B) {
					// Dispose on ESC key press
					dispose();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// NO-OP
			}
		});
		// Handle mouse clicks
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				handleTreeViewerMouseUp(tree, e);
			}
		});
		// Handle mouse move events
		tree.addMouseMoveListener(new QuickOutlineMouseMoveListener(treeViewer));
		// Handle widget selection events
		tree.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// NO-OP
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});

	}

	private class QuickOutlineMouseMoveListener implements MouseMoveListener {
		private TreeItem fLastItem;

		private TreeViewer fTreeViewer;

		public QuickOutlineMouseMoveListener(TreeViewer treeViewer) {
			fLastItem = null;
			fTreeViewer = treeViewer;
		}

		@Override
		public void mouseMove(MouseEvent e) {
			Tree tree = fTreeViewer.getTree();
			if (tree.equals(e.getSource())) {
				Object o = tree.getItem(new Point(e.x, e.y));
				if (o instanceof TreeItem) {
					if (!o.equals(fLastItem)) {
						fLastItem = (TreeItem) o;
						tree.setSelection(new TreeItem[] { fLastItem });
					} else if (e.y < tree.getItemHeight() / 4) {
						// Scroll up
						Point p = tree.toDisplay(e.x, e.y);
						Item item = fTreeViewer.scrollUp(p.x, p.y);
						if (item instanceof TreeItem) {
							fLastItem = (TreeItem) item;
							tree.setSelection(new TreeItem[] { fLastItem });
						}
					} else if (e.y > tree.getBounds().height - tree.getItemHeight() / 4) {
						// Scroll down
						Point p = tree.toDisplay(e.x, e.y);
						Item item = fTreeViewer.scrollDown(p.x, p.y);
						if (item instanceof TreeItem) {
							fLastItem = (TreeItem) item;
							tree.setSelection(new TreeItem[] { fLastItem });
						}
					}
				}
			}
		}

	}

	/**
	 * Handles mouse up action for the tree viewer
	 * 
	 * @param tree
	 *            current tree
	 * @param e
	 *            mouse event
	 */
	private void handleTreeViewerMouseUp(final Tree tree, MouseEvent e) {
		// Ensure a selection was made, the first mouse button was
		// used and the event happened in the tree
		if ((tree.getSelectionCount() < 1) || (e.button != 1) || !tree.equals(e.getSource())) {
			return;
		}
		// Selection is made in the selection changed listener
		Object object = tree.getItem(new Point(e.x, e.y));
		TreeItem selection = tree.getSelection()[0];
		if (selection.equals(object)) {
			gotoSelectedElement();
		}
	}

	/**
	 * Performs passing to selected element with use of outline page
	 * functionality
	 */
	private void gotoSelectedElement() {
		Object selectedElement = getSelectedElement();
		if (selectedElement == null) {
			return;
		}
		dispose();
		if (selectedElement instanceof NavigationBarItem) {
			if (tsFile instanceof IIDETypeScriptFile) {
				EditorUtils.openInEditor(((IFile) ((IIDETypeScriptFile) tsFile).getResource()),
						(NavigationBarItem) selectedElement);
			}
		}
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		// Applies only to dialog title - not body. See createDialogArea
		// Create the text widget
		createFilterText(parent);
		// Add listeners to the text widget
		createListenersFilterText();
		// Return the text widget
		return filterText;
	}

	/**
	 * @param parent
	 *            parent control
	 */
	private void createFilterText(Composite parent) {
		// Create the widget
		filterText = new Text(parent, SWT.NONE);
		// Set the font
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		// Create the layout
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.CENTER;
		filterText.setLayoutData(data);
	}

	/**
	 * 
	 */
	private void createListenersFilterText() {
		// Handle key events
		filterText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					// Return key was pressed
					gotoSelectedElement();
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					// Down key was pressed
					treeViewer.getTree().setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					// Up key was pressed
					treeViewer.getTree().setFocus();
				} else if (e.character == 0x1B) {
					// Escape key was pressed
					dispose();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// NO-OP
			}
		});
		// Handle text modify events
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText();
				int length = text.length();
				if (length > 0) {
					// Append a '*' pattern to the end of the text value if it
					// does not have one already
					if (text.charAt(length - 1) != '*') {
						text = text + '*';
					}
					// Prepend a '*' pattern to the beginning of the text value
					// if it does not have one already
					if (text.charAt(0) != '*') {
						text = '*' + text;
					}
				}
				// Set and update the pattern
				setMatcherString(text, true);
			}
		});
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 * <p>
	 * The following characters have special meaning: ? => any character * =>
	 * any string
	 * </p>
	 *
	 * @param pattern
	 *            the pattern
	 * @param update
	 *            <code>true</code> if the viewer should be updated
	 */
	private void setMatcherString(String pattern, boolean update) {
		if (pattern.length() == 0) {
			stringMatcher = null;
		} else {
			stringMatcher = new StringMatcher(pattern, true, false);
		}
		// Update the name pattern filter on the tree viewer
		namePatternFilter.setStringMatcher(stringMatcher);
		// Update the tree viewer according to the pattern
		if (update) {
			stringMatcherUpdated();
		}
	}

	/**
	 * The string matcher has been modified. The default implementation
	 * refreshes the view and selects the first matched element
	 */
	private void stringMatcherUpdated() {
		// Refresh the tree viewer to re-filter
		treeViewer.getControl().setRedraw(false);
		treeViewer.refresh();
		treeViewer.expandAll();
		selectFirstMatch();
		treeViewer.getControl().setRedraw(true);
	}

	/**
	 * Selects the first element in the tree which matches the current filter
	 * pattern.
	 */
	private void selectFirstMatch() {
		Tree tree = treeViewer.getTree();
		Object element = findFirstMatchToPattern(tree.getItems());
		if (element != null) {
			treeViewer.setSelection(new StructuredSelection(element), true);
		} else {
			treeViewer.setSelection(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Recursively searches the first element in the tree which matches the
	 * current filter pattern.
	 * 
	 * @param items
	 *            tree root items
	 * @return tree element
	 */
	private Object findFirstMatchToPattern(TreeItem[] items) {
		// Match the string pattern against labels
		ILabelProvider labelProvider = (ILabelProvider) treeViewer.getLabelProvider();
		// Process each item in the tree
		for (int i = 0; i < items.length; i++) {
			Object element = items[i].getData();
			// Return the first element if no pattern is set
			if (stringMatcher == null) {
				return element;
			}
			// Return the element if it matches the pattern
			if (element != null) {
				String label = labelProvider.getText(element);
				if (stringMatcher.match(label)) {
					return element;
				}
			}
			// Recursively check the elements children for a match
			element = findFirstMatchToPattern(items[i].getItems());
			// Return the child element match if found
			if (element != null) {
				return element;
			}
		}
		// No match found
		return null;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		// Note: We do not reuse the dialog
		treeViewer = null;
		filterText = null;
	}

	@Override
	public void setInput(Object input) {
		// Input comes from PDESourceInfoProvider.getInformation2()
		// The input should be a model object of some sort
		// Turn it into a structured selection and set the selection in the tree
		if (input != null && treeViewer != null) {
			treeViewer.setSelection(new StructuredSelection(input));
		}
	}

	@Override
	public boolean hasContents() {
		if ((treeViewer == null) || (treeViewer.getInput() == null)) {
			return false;
		}
		return true;
	}

	@Override
	public void setInformation(String information) {
		// Ignore
		// See IInformationControlExtension2
	}

	@Override
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		// Ignore
	}

	@Override
	public Point computeSizeHint() {
		// Return the shell's size
		// Note that it already has the persisted size if persisting is enabled.
		return getShell().getSize();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			open();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	@Override
	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	@Override
	public void setLocation(Point location) {
		/*
		 * If the location is persisted, it gets managed by PopupDialog - fine.
		 * Otherwise, the location is computed in Window#getInitialLocation,
		 * which will center it in the parent shell / main monitor, which is
		 * wrong for two reasons: - we want to center over the editor / subject
		 * control, not the parent shell - the center is computed via the
		 * initalSize, which may be also wrong since the size may have been
		 * updated since via min/max sizing of
		 * AbstractInformationControlManager. In that case, override the
		 * location with the one computed by the manager. Note that the call to
		 * constrainShellSize in PopupDialog.open will still ensure that the
		 * shell is entirely visible.
		 */
		if (!getPersistLocation() || (getDialogSettings() == null)) {
			getShell().setLocation(location);
		}
	}

	@Override
	public void dispose() {
		if (tsFile != null) {
			this.tsFile.removeNavbarListener(this);
		}
		close();
	}

	@Override
	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	@Override
	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);
	}

	@Override
	public void setForegroundColor(Color foreground) {
		applyForegroundColor(foreground, getContents());
	}

	@Override
	public void setBackgroundColor(Color background) {
		applyBackgroundColor(background, getContents());
	}

	@Override
	public boolean isFocusControl() {
		if (treeViewer.getControl().isFocusControl() || filterText.isFocusControl()) {
			return true;
		}
		return false;
	}

	@Override
	public void setFocus() {
		getShell().forceFocus();
		filterText.setFocus();
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		getShell().addFocusListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		getShell().removeFocusListener(listener);
	}

	/**
	 * @author Asya Vorobyova
	 *
	 */
	private class QuickOutlineNamePatternFilter extends ViewerFilter {

		/**
		 * 
		 */
		private StringMatcher stringMatcher;

		/**
		 * Constructor
		 */
		public QuickOutlineNamePatternFilter() {
			stringMatcher = null;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			// Element passes the filter if the string matcher is undefined or
			// the
			// viewer is not a tree viewer
			if ((stringMatcher == null) || !(viewer instanceof TreeViewer)) {
				return true;
			}
			TreeViewer aTreeViewer = (TreeViewer) viewer;
			// Match the pattern against the label of the given element
			String matchName = ((ILabelProvider) aTreeViewer.getLabelProvider()).getText(element);
			// Element passes the filter if it matches the pattern
			if ((matchName != null) && stringMatcher.match(matchName)) {
				return true;
			}
			// Determine whether the element has children that pass the filter
			return hasUnfilteredChild(aTreeViewer, element);
		}

		/**
		 * @param viewer
		 * @param element
		 * @return if the element has children that pass the filter
		 */
		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			// No point calling hasChildren() because the operation is the same
			// cost
			// as getting the children
			// If the element has a child that passes the filter, then we want
			// to
			// keep the parent around - even if it does not pass the filter
			// itself
			Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
			for (int i = 0; i < children.length; i++) {
				if (select(viewer, element, children[i])) {
					return true;
				}
			}
			// Element does not pass the filter
			return false;
		}

		/**
		 * @param newStringMatcher
		 *            a string matcher to be used
		 */
		public void setStringMatcher(StringMatcher newStringMatcher) {
			stringMatcher = newStringMatcher;
		}

	}

	@Override
	public void navBarChanged(final List<NavigationBarItem> items) {
		if (treeViewer != null) {
			treeViewer.getTree().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					treeViewer.setInput(items);
				}
			});
		}
	}
}