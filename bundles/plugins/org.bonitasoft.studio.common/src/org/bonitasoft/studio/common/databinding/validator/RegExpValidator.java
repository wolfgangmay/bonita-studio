/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.databinding.validator;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;


public class RegExpValidator implements IValidator {

    private final String errorMessage;
    private final String regex;

    public RegExpValidator(final String errorMessage, final String regex) {
        this.errorMessage = errorMessage;
        this.regex = regex;
    }

    @Override
    public IStatus validate(final Object input) {
        if (input != null && !input.toString().matches(regex)) {
            return ValidationStatus.error(errorMessage);
        }
        return ValidationStatus.ok() ;
    }

}
