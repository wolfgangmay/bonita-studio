/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.common.ui.jface.databinding.converter;

import org.bonitasoft.bpm.model.process.Element;
import org.eclipse.core.databinding.conversion.Converter;

public class ElementToStringConverter extends Converter {

    public ElementToStringConverter() {
        super(Element.class, String.class);
    }

    @Override
    public Object convert(final Object fromObject) {
        return fromObject == null ? "" : ((Element) fromObject).getName();
    }

}
