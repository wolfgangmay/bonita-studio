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
package org.bonitasoft.studio.contract.ui.property;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.or;

import org.bonitasoft.bpm.model.process.ContractContainer;
import org.bonitasoft.bpm.model.process.Lane;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.viewers.IFilter;

/**
 * @author Romain Bioteau
 */
public class ContractContainerFilter implements IFilter {

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
     */
    @Override
    public boolean select(final Object editPart) {
        if (editPart instanceof IGraphicalEditPart) {
            return or(instanceOf(ContractContainer.class), instanceOf(Lane.class))
                    .apply(((IGraphicalEditPart) editPart).resolveSemanticElement());
        }
        return false;
    }

}
