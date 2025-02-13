/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.document.ui.control;

import org.bonitasoft.studio.document.ui.validator.SingleContractInputValidator;
import org.bonitasoft.bpm.model.process.Document;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.swt.widgets.Composite;

public class SingleFileContractInputSelectionComposite extends FileContractInputSelectionComposite {

    public SingleFileContractInputSelectionComposite(final Composite parent) {
        super(parent);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.bonitasoft.studio.document.ui.control.FileContractInputSelectionComposite#createContractInputParameter(org.bonitasoft.bpm.model.process.Document,
     * org.eclipse.core.databinding.observable.value.IObservableValue, org.eclipse.jface.databinding.viewers.IViewerObservableValue)
     */
    @Override
    protected ValidationStatusProvider createContractInputParameter(final Document document, final IObservableValue observeInput,
            final IObservableValue observeSingleSelection) {
        return new SingleContractInputValidator(document, observeSingleSelection, observeInput);
    }

}
