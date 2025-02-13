/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.studio.designer.core.repository;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;

/**
 * @author Romain Bioteau
 */
public class WebFragmentRepositoryStore extends WebArtifactRepositoryStore<WebFragmentFileStore> {

    private static final Set<String> extensions = new HashSet<>();
    public static final String JSON_EXTENSION = "json";
    public static final String WEB_FRAGMENT_REPOSITORY_NAME = "web_fragments";

    static {
        extensions.add(JSON_EXTENSION);
    }

    @Override
    public String getName() {
        return WEB_FRAGMENT_REPOSITORY_NAME;
    }

    @Override
    public Set<String> getCompatibleExtensions() {
        return extensions;
    }

    @Override
    public WebFragmentFileStore createRepositoryFileStore(final String fileName) {
        IFolder folder = getResource().getFolder(fileName);
        if (folder.exists()) {
            return folder.getFile(fileName + ".json").exists() ? new WebFragmentFileStore(fileName, this) : null;
        }
        return new WebFragmentFileStore(fileName, this);
    }

}
