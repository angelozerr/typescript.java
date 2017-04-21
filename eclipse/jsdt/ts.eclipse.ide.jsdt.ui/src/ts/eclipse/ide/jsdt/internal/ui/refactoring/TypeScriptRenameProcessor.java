package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Position;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import ts.client.TextSpan;
import ts.client.rename.RenameResponseBody;
import ts.client.rename.SpanGroup;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.jsdt.core.JSDTTypeScriptCorePlugin;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;

public class TypeScriptRenameProcessor extends RenameProcessor {

	private final ITypeScriptFile tsFile;
	private final int offset;
	private final String oldName;

	private String newName;

	public TypeScriptRenameProcessor(ITypeScriptFile tsFile, int offset, String oldName) {
		this.tsFile = tsFile;
		this.offset = offset;
		this.oldName = oldName;
	}

	@Override
	public Object[] getElements() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return "ts.eclipse.ide.core.refactoring.rename";
	}

	@Override
	public String getProcessorName() {
		return "Rename TypeScript Element";
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return true;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			ITypeScriptProject tsProject = tsFile.getProject();
			RenameResponseBody rename = tsFile.rename(offset, false, false).get(1000, TimeUnit.MILLISECONDS);
			List<SpanGroup> locs = rename.getLocs();

			List<Change> fileChanges = new ArrayList<>();
			for (SpanGroup loc : locs) {
				IFile file = WorkbenchResourceUtil.findFileFromWorkspace(loc.getFile());
				TextFileChange change = new TextFileChange(file.getName(), file);
				change.setEdit(new MultiTextEdit());
				change.setTextType("ts");

				List<TextSpan> spans = loc.getLocs();
				for (TextSpan textSpan : spans) {
					Position position = EditorUtils.getPosition(file, textSpan);
					ReplaceEdit edit = new ReplaceEdit(position.offset, position.length, this.newName);
					change.addEdit(edit);
				}
				fileChanges.add(change);
			}
			return new CompositeChange("Rename TypeScript Element",
					fileChanges.toArray(new Change[fileChanges.size()]));
		} catch (OperationCanceledException e) {
			throw e;
		} catch (Exception e) {
			throw new CoreException(
					new Status(IStatus.ERROR, JSDTTypeScriptCorePlugin.PLUGIN_ID, "Error while rename", e));
		}
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return null;
	}

	public String getOldName() {
		return this.oldName;
	}

	public void setNewName(String newName) {
		Assert.isNotNull(newName);
		this.newName = newName;
	}

	public int getSaveMode() {
		return 1; // RefactoringSaveHelper.SAVE_NOTHING;
	}
}
