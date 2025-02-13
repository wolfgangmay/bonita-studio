/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.studio.application.validator;

import org.bonitasoft.studio.application.i18n.Messages;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

public class ThemeExtensionTypeValidator extends RestApiExtensionExtensionTypeValidator {

    private static final String THEME_EXTENSION_CONTENT_TYPE = "theme";

    @Override
    protected String getExpectedContentType() {
        return THEME_EXTENSION_CONTENT_TYPE;
    }

    @Override
    protected IStatus createStatus(boolean isRestApi) {
        return isRestApi
                ? ValidationStatus.ok()
                : ValidationStatus.error(Messages.extensionIsNotAThemeExtension);
    }

}
