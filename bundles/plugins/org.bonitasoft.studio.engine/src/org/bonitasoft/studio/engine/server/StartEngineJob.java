/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

package org.bonitasoft.studio.engine.server;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class StartEngineJob extends Job {

    private static final Object FAMILY = RepositoryManager.class;
    private IRepository repository;

    public StartEngineJob(String name, IRepository repository) {
        super(name);
        this.repository = repository;
    }

    @Override
    public boolean belongsTo(Object family) {
        return FAMILY.equals(family);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            BOSEngineManager.getInstance(monitor).start(repository);
        } catch (Exception e) {
            BonitaStudioLog.error(e);
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

}
