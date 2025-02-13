/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.studio.common.repository.model;

import java.util.Map;

import org.bonitasoft.engine.session.APISession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IDeployable {

    /**
     * Shortcut method that must run a deploy and is responsible to display the result in the UI
     */
    public void deployInUI();

    /**
     * Deploy and return the status of the deployment
     * 
     * @param session engine active session
     * @param options deployment options
     * @param monitor
     * @return status of the deployment
     */
    public IStatus deploy(APISession session, Map<String, Object> options, IProgressMonitor monitor);

}
