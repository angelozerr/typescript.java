/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *
 */
package ts.eclipse.ide.internal.ui.wizards;

import org.eclipse.jface.viewers.LabelProvider;

import ts.repository.ITypeScriptRepository;

/**
 * Label provider for {@link ITypeScriptRepository}.
 *
 */
public class TypeScriptRepositoryLabelProvider extends LabelProvider {

	private final boolean tslint;

	public TypeScriptRepositoryLabelProvider() {
		this(false);
	}

	public TypeScriptRepositoryLabelProvider(boolean tslint) {
		this.tslint = tslint;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ITypeScriptRepository) {
			if (tslint) {
				return ((ITypeScriptRepository) element).getTslintName();
			}
			return ((ITypeScriptRepository) element).getName();
		}
		return super.getText(element);
	}
}
