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
package org.bonitasoft.studio.document.core.repository;

import org.bonitasoft.studio.common.repository.core.migration.report.MigrationReport;
import org.bonitasoft.studio.common.repository.store.AbstractRepositoryStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.edapt.migration.MigrationException;

/**
 * @author Romain Bioteau
 */
public class DocumentRepositoryStore extends AbstractRepositoryStore<DocumentFileStore> {

    private static final String STORE_NAME = "attachments";
    public static final String PATH_IN_BAR = "attachments/";

    @Override
    public DocumentFileStore createRepositoryFileStore(final String fileName) {
        return new DocumentFileStore(fileName, this);
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public MigrationReport migrate(IProgressMonitor monitor) throws CoreException, MigrationException {
        //DO NOTHING
        return MigrationReport.emptyReport();
    }
}
