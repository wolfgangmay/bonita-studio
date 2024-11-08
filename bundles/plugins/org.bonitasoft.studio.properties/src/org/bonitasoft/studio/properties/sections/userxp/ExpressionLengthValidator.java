/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.sections.userxp;

import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.expression.editor.provider.IExpressionValidator;
import org.bonitasoft.bpm.model.expression.Expression;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;

public class ExpressionLengthValidator implements IExpressionValidator {

    private Expression inputExpression;
    private final int maxLength;

    public ExpressionLengthValidator(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public IStatus validate(final Object value) {
        if (ExpressionConstants.CONSTANT_TYPE.equals(inputExpression.getType())) {
            if (value instanceof String && ((String) value).length() > maxLength) {
                return ValidationStatus.error(Messages.bind(Messages.errorDisplayLabelMaxLength, maxLength));
            }
        }
        return Status.OK_STATUS;
    }

    @Override
    public void setInputExpression(final Expression inputExpression) {
        this.inputExpression = inputExpression;
    }

    @Override
    public void setDomain(final EditingDomain domain) {
    }

    @Override
    public void setContext(final EObject context) {
    }

    @Override
    public boolean isRelevantForExpressionType(final String type) {
        return ExpressionConstants.CONSTANT_TYPE.equals(type);
    }

}
