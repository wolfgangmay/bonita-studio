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
package org.bonitasoft.studio.engine.operation;

import java.io.UnsupportedEncodingException;

import org.bonitasoft.bpm.model.process.AbstractProcess;
import org.eclipse.core.runtime.IProgressMonitor;

public class TaskListURLBuilder extends AbstractProcessRelatedURLBuilder {

    private static final String TASKLIST_URL_TEMPLATE = "apps/%s/task-list/?_l=%s";

    public TaskListURLBuilder(final AbstractProcess process, final String configurationId) {
        super(process, configurationId);
    }

    @Override
    protected String getRedirectURL(final String locale, final IProgressMonitor monitor)
            throws UnsupportedEncodingException {
        return String.format(TASKLIST_URL_TEMPLATE,
                userAppToken(),
                locale);
    }

}
