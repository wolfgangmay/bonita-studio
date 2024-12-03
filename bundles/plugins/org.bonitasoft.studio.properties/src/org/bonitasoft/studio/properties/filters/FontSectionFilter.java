/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.properties.filters;

import org.bonitasoft.bpm.model.process.Activity;
import org.bonitasoft.bpm.model.process.BoundaryEvent;
import org.bonitasoft.bpm.model.process.Connection;
import org.bonitasoft.bpm.model.process.Event;
import org.bonitasoft.bpm.model.process.Gateway;
import org.bonitasoft.bpm.model.process.Lane;
import org.bonitasoft.bpm.model.process.MessageFlow;
import org.bonitasoft.bpm.model.process.Pool;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.viewers.IFilter;

/**
 * @author Romain Bioteau
 *
 */
public class FontSectionFilter implements IFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object object) {
		if (object instanceof IGraphicalEditPart) {
			IGraphicalEditPart editPart = (IGraphicalEditPart) object;
			Object model = editPart.resolveSemanticElement();
			return model instanceof BoundaryEvent || model instanceof Gateway || model instanceof Event || model instanceof Activity || model instanceof Lane ||model instanceof Pool  || model instanceof Connection || model instanceof MessageFlow;
		}
		return false;
	}

}
