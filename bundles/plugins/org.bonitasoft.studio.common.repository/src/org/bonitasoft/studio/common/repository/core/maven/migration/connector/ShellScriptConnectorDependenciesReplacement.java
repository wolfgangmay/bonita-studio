/**
 * Copyright (C) 2021 BonitaSoft S.A.
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
package org.bonitasoft.studio.common.repository.core.maven.migration.connector;

import org.bonitasoft.studio.common.repository.core.maven.migration.BonitaJarDependencyReplacement;

public class ShellScriptConnectorDependenciesReplacement extends BonitaJarDependencyReplacement {

    public ShellScriptConnectorDependenciesReplacement() {
        super(dependency(CONNECTOR_GROUP_ID, "bonita-connector-shell-script", "1.1.2"),
                "bonita-connector-shell-script-1.1.2.jar", 
                "bonita-connector-shell-script-1.1.0.jar", 
                "bonita-connector-scripting-1.1.0.jar",
                "bonita-connector-scripting-shell-impl-.*.jar");
    }

}
