/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.repository.provider;

import org.bonitasoft.studio.common.Strings;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.ui.IDisplayable;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 */
public class FileStoreLabelProvider extends LabelProvider {

    @Override
    public String getText(final Object element) {
        IDisplayable displayable = Adapters.adapt(element, IDisplayable.class);
        if (displayable != null) {
            String displayName = displayable.getDisplayName();
            if (Strings.isNullOrEmpty(displayName) && element instanceof IRepositoryFileStore<?>) {
                displayName = ((IRepositoryFileStore) element).getName();
            }
            return displayName;
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(final Object element) {
        IDisplayable displayable = Adapters.adapt(element, IDisplayable.class);
        if (displayable != null) {
            return displayable.getIcon();
        }
        return super.getImage(element);
    }
}
