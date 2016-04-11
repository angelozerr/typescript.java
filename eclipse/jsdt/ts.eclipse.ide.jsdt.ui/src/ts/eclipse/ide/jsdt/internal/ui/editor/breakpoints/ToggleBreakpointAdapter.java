/*******************************************************************************
 * Copyright (c) 2010-2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genuitec LLC - adapted for TypeScript editor
 *******************************************************************************/
package ts.eclipse.ide.jsdt.internal.ui.editor.breakpoints;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.debug.core.breakpoints.IJavaScriptBreakpoint;
import org.eclipse.wst.jsdt.debug.core.breakpoints.IJavaScriptLineBreakpoint;
import org.eclipse.wst.jsdt.debug.core.model.JavaScriptDebugModel;

/**
 * Adapter for toggling JavaScript breakpoints in the TypeScript editor
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {

	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		//do nothing
	}
	
	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		//do nothing
	}

	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if(selection instanceof ITextSelection) {
			ITextEditor textEditor = getTextEditor(part);
			if(textEditor == null) {
				reportToStatusLine(part, "No editor could be found for the associated part");
				return;
			}
			toggleLineBreakpoint(part, (ITextSelection) selection, ((TextSelection)selection).getStartLine()+1);
		}
	}
	
	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection) throws CoreException {
		if (!(part instanceof IEditorPart) || !(selection instanceof ITextSelection)) {
			return;
		}

		ITextEditor textEditor = (ITextEditor) part.getAdapter(ITextEditor.class);
		if (textEditor != null) {
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			if (document != null) {
				int lineNumber;
				try {
					lineNumber = document.getLineOfOffset(((ITextSelection) selection).getOffset());
					IResource res = getResource((IEditorPart) part);
					if (res != null) {
						addBreakpoint(res, document, lineNumber + 1);
					}
				}
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void toggleLineBreakpoint(final IWorkbenchPart part, final ITextSelection selection, final int linenumber) {
		Job job = new Job("Toggle Line Breakpoints") {
            protected IStatus run(IProgressMonitor monitor) {
            	try {
            		ITextEditor editor = getTextEditor(part);
					if(editor != null && part instanceof IEditorPart) {
						IResource resource = getResource((IEditorPart)part);
						if(resource == null) {
							resource = getResource((IEditorPart)part);
							reportToStatusLine(part, "Failed to create Javascript line breakpoint - the resource could no be computed");
							return Status.CANCEL_STATUS;
						}
						IBreakpoint bp = lineBreakpointExists(resource, linenumber);
						if(bp != null) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(bp, true);
							return Status.OK_STATUS;
						}
						IDocumentProvider documentProvider = editor.getDocumentProvider();
						IDocument document = documentProvider.getDocument(editor.getEditorInput());
						int charstart = -1, charend = -1;
						try {
							IRegion line = document.getLineInformation(linenumber - 1);
							charstart = line.getOffset();
							charend = charstart + line.getLength();
						}
						catch (BadLocationException ble) {}
						HashMap<String, String> attributes = new HashMap<String, String>();
						attributes.put(IJavaScriptBreakpoint.TYPE_NAME, null);
						attributes.put(IJavaScriptBreakpoint.SCRIPT_PATH, resource.getFullPath().makeAbsolute().toString());
						attributes.put(IJavaScriptBreakpoint.ELEMENT_HANDLE, null);
						JavaScriptDebugModel.createLineBreakpoint(resource, linenumber, charstart, charend, attributes, true);
						return Status.OK_STATUS;
					}
					reportToStatusLine(part, "Failed to create Javascript line breakpoint");
					return Status.CANCEL_STATUS;
	            }
	        	catch(CoreException ce) {
	        		return ce.getStatus();
        		}
        	}
		};
		job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.schedule();
	}

	void addBreakpoint(IResource resource, IDocument document, int linenumber) throws CoreException {
		IBreakpoint bp = lineBreakpointExists(resource, linenumber);
		if(bp != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(bp, true);
		}
		int charstart = -1, charend = -1;
		try {
			IRegion line = document.getLineInformation(linenumber - 1);
			charstart = line.getOffset();
			charend = charstart + line.getLength();
		}
		catch (BadLocationException ble) {}
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put(IJavaScriptBreakpoint.TYPE_NAME, null);
		attributes.put(IJavaScriptBreakpoint.SCRIPT_PATH, resource.getFullPath().makeAbsolute().toString());
		attributes.put(IJavaScriptBreakpoint.ELEMENT_HANDLE, null);
		JavaScriptDebugModel.createLineBreakpoint(resource, linenumber, charstart, charend, attributes, true);
	}

	void reportToStatusLine(final IWorkbenchPart part, final String message) {
		getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
				IEditorStatusLine statusLine = (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
		        if (statusLine != null) {
		            if (message != null) {
		                statusLine.setMessage(true, message, null);
		            } else {
		                statusLine.setMessage(true, null, null);
		            }
		        }
            }
		});
	}

	Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	IBreakpoint lineBreakpointExists(IResource resource, int linenumber) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JavaScriptDebugModel.MODEL_ID);
		IJavaScriptLineBreakpoint breakpoint = null;
		for (int i = 0; i < breakpoints.length; i++) {
			if(breakpoints[i] instanceof IJavaScriptLineBreakpoint) {
				breakpoint = (IJavaScriptLineBreakpoint) breakpoints[i];
				try {
					if(IJavaScriptLineBreakpoint.MARKER_ID.equals(breakpoint.getMarker().getType()) &&
						resource.equals(breakpoint.getMarker().getResource()) &&
						linenumber == breakpoint.getLineNumber()) {
						return breakpoint;
					}
				} catch (CoreException e) {}
			}
		}
		return null;
	}

    IResource getResource(IEditorPart editor) {
        IEditorInput editorInput = editor.getEditorInput();
        return (IResource) editorInput.getAdapter(IFile.class);
    }

    ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return (ITextEditor) part.getAdapter(ITextEditor.class);
    }
    
}
