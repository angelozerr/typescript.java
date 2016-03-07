package ts.eclipse.ide.ui.hyperlink;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.hyperlink.TypeScriptHyperlink;
import ts.eclipse.ide.ui.JavaWordFinder;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.utils.EditorUtils;

public class TypeScriptHyperLinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		// Get resource from the given text viewer.
		IResource resource = getResource(textViewer);
		if (resource == null) {
			return null;
		}
		if (TypeScriptCorePlugin.canConsumeTsserver(resource)) {
			// the project of the resource has typescript nature, execute
			// typescript
			// hyperlink.
			try {
				IProject project = resource.getProject();
				IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(project);
				IDocument document = textViewer.getDocument();
				IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);
				IRegion wordRegion = JavaWordFinder.findWord(document, region.getOffset());

				TypeScriptHyperlink hyperlink = new TypeScriptHyperlink(tsFile, wordRegion);
				if (hyperlink.isValid()) {
					IHyperlink[] hyperlinks = new IHyperlink[1];
					hyperlinks[0] = hyperlink;
					return hyperlinks;
				}
				return null;

			} catch (Exception e) {
				TypeScriptUIPlugin.log("Error while TypeScript hyperlink", e);
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IResource} from the given text viewer and null
	 * otherwise.
	 * 
	 * @param textViewer
	 * @return the {@link IResource} from the given text viewer and null
	 *         otherwise.
	 */
	protected IResource getResource(ITextViewer textViewer) {
		ITextEditor textEditor = (ITextEditor) getAdapter(ITextEditor.class);
		if (textEditor != null) {
			return EditorUtils.getResource(textEditor);
		}
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(textViewer.getDocument());
		if (textFileBuffer != null) {
			IPath location = textFileBuffer.getLocation();
			return ResourcesPlugin.getWorkspace().getRoot().findMember(location);
		}
		return null;
	}

}
