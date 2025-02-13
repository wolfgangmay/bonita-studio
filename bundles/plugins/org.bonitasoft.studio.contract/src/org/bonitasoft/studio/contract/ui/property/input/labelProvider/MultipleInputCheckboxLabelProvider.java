/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.contract.ui.property.input.labelProvider;

import org.bonitasoft.studio.common.ui.jface.databinding.CustomCheckBoxColumnLabelProvider;
import org.bonitasoft.bpm.model.process.ContractInput;
import org.bonitasoft.bpm.model.process.ProcessPackage;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.viewers.ColumnViewer;

/**
 * @author Romain Bioteau
 */
public class MultipleInputCheckboxLabelProvider extends CustomCheckBoxColumnLabelProvider {

    public MultipleInputCheckboxLabelProvider(final ColumnViewer viewer, final IObservableSet knowElements) {
        super(viewer, ProcessPackage.Literals.CONTRACT_INPUT__MULTIPLE, knowElements);
    }

    @Override
    protected boolean isSelected(final Object element) {
        if (element instanceof ContractInput) {
            return ((ContractInput) element).isMultiple();
        }
        return false;
    }

}
