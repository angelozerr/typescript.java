/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.core.resources.problems;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.problems.IProblemChangeListener;
import ts.eclipse.ide.core.resources.problems.IProblemManager;

/**
 * Implementation of {@link IProblemManager}.
 */
public class ProblemManager implements IProblemManager {

	private static final ProblemManager INSTANCE = new ProblemManager();

	public static ProblemManager getInstance() {
		return INSTANCE;
	}

	private final IResourceChangeListener resourceChangeListener;
	private final List<IProblemChangeListener> listeners;

	private ProblemManager() {
		this.resourceChangeListener = new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				handleResourceChanged(event);
			}
		};

		this.listeners = new CopyOnWriteArrayList<>();
	}

	public synchronized void shutdown() {
		this.listeners.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
	}

	@Override
	public synchronized void addProblemChangedListener(IProblemChangeListener listener) {
		this.listeners.add(Objects.requireNonNull(listener));
		if (this.listeners.size() == 1) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		}
	}

	@Override
	public synchronized void removeProblemChangedListener(IProblemChangeListener listener) {
		this.listeners.remove(listener);
		if (this.listeners.isEmpty()) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
	}

	private void handleResourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}

		// Find resourced whose problems changed and report them to listeners
		Set<IResource> changedResources = new HashSet<>();
		try {
			delta.accept(new ProblemMarkerDeltaVisitor(changedResources));
		} catch (CoreException e) {
			TypeScriptCorePlugin.logError(e);
		}
		if (!changedResources.isEmpty()) {
			notifyListeners(Collections.unmodifiableSet(changedResources));
		}
	}

	private static class ProblemMarkerDeltaVisitor implements IResourceDeltaVisitor {

		private final Set<IResource> changedResources;

		public ProblemMarkerDeltaVisitor(Set<IResource> changedResources) {
			this.changedResources = Objects.requireNonNull(changedResources);
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
				IProject project = (IProject) resource;
				if (!project.isAccessible()) {
					return false; // skip closed projects
				}
			}
			checkInvalidate(delta, resource);
			return true;
		}

		private void checkInvalidate(IResourceDelta delta, IResource resource) {
			int kind = delta.getKind();
			if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED
					|| (kind == IResourceDelta.CHANGED && isProblemDelta(delta))) {

				// Invalidate the resource and all its ancestors
				for (IResource r = resource; r != null; r = r.getParent()) {
					boolean added = changedResources.add(r);
					if (!added) {
						break;
					}
				}
			}
		}

		private boolean isProblemDelta(IResourceDelta delta) {
			if ((delta.getFlags() & IResourceDelta.MARKERS) == 0) {
				return false;
			}
			for (IMarkerDelta markerDelta : delta.getMarkerDeltas()) {
				if (markerDelta.isSubtypeOf(IMarker.PROBLEM)) {

					// Detect added/removed problem markers
					int kind = markerDelta.getKind();
					if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED) {
						return true;
					}

					// Detect changes in problem marker severity
					int oldSeverity = markerDelta.getAttribute(IMarker.SEVERITY, -1);
					int newSeverity = markerDelta.getMarker().getAttribute(IMarker.SEVERITY, -1);
					if (newSeverity != oldSeverity) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private void notifyListeners(Set<IResource> changedResources) {
		for (IProblemChangeListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.problemsChanged(changedResources);
				}

				@Override
				public void handleException(Throwable exception) {
					// logged by SafeRunner
				}
			});
		}
	}

}
