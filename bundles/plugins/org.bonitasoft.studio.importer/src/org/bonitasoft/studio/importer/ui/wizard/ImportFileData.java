/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.studio.importer.ui.wizard;

import org.bonitasoft.studio.importer.ImporterFactory;

public class ImportFileData {

    private String filePath;
    private ImporterFactory importerFactory;

    private String selectedRepositoryName;

    public String getSelectedRepositoryName() {
        return selectedRepositoryName;
    }

    public void setSelectedRepositoryName(String selectedRepositoryName) {
        this.selectedRepositoryName = selectedRepositoryName;
    }

    public ImporterFactory getImporterFactory() {
        return importerFactory;
    }

    public void setImporterFactory(ImporterFactory importerFactory) {
        this.importerFactory = importerFactory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
