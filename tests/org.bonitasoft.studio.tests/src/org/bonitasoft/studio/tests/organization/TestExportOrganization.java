/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.studio.tests.organization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.identity.organization.repository.OrganizationRepositoryStore;
import org.bonitasoft.studio.tests.util.InitialProjectRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestExportOrganization {
    
    @Rule
    public InitialProjectRule projectRule = InitialProjectRule.INSTANCE;
    
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    
    @Test
    public void testExportDefaultOrganization() throws Exception {
        final IRepositoryStore<? extends IRepositoryFileStore> organizationStore = RepositoryManager.getInstance()
                .getRepositoryStore(OrganizationRepositoryStore.class);
        final String fileName = "ACME." + OrganizationRepositoryStore.ORGANIZATION_EXT;
        IRepositoryFileStore organization = organizationStore.getChild(fileName, true);
        File destFolder = tmpFolder.newFolder();
        assertNotNull("organization " + fileName + " does not exist in repository", organization);
        File f = new File(destFolder, "ACME.XML");
        final String exportPath = f.getAbsolutePath();
        organization.export(exportPath);
        File export = new File(exportPath);
        assertNotNull("organization was not exported", export);
        assertTrue(export.getAbsolutePath() + " organization was not exported", export.exists());
        f.deleteOnExit();
        export.deleteOnExit();
    }

}
