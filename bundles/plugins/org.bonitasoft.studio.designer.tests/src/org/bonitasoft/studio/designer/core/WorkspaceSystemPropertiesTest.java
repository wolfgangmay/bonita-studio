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
package org.bonitasoft.studio.designer.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WorkspaceSystemPropertiesTest {

    private WorkspaceSystemProperties workspaceSystemProperties;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();


    private Path projectPath;

    @Before
    public void setUp() throws Exception {
        projectPath = Paths.get("myProject");
        workspaceSystemProperties = new WorkspaceSystemProperties(projectPath);
    }


    @Test
    public void should_aSystemProperty_return_a_well_formatted_system_property_argument() throws Exception {
        assertThat(WorkspaceSystemProperties.aSystemProperty("aProperty.name", "myValue")).isEqualTo("-DaProperty.name=myValue");
    }

    @Test
    public void should_getRestAPIURL_return_the_rest_resource_server_url() throws Exception {
        assertThat(workspaceSystemProperties.getRestAPIURL(6666)).isEqualTo(
                String.format("-Ddesigner.workspace.apiUrl=http://%s:6666/api/workspace",InetAddress.getByName(null).getHostAddress()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_an_IllegalArgumentException_if_profiles_are_null() throws Exception {
        workspaceSystemProperties.activateSpringProfile();
    }

    @Test
    public void should_activateSpringProfile_return_a_valid_spring_environment_property() throws Exception {
        assertThat(workspaceSystemProperties.activateSpringProfile("dev", null, "prod")).isEqualTo("-Dspring.profiles.active=dev,prod");
    }
    
    @Test
    public void should_create_workspacePathLocation_uid_environment_property() throws Exception {
        assertThat(workspaceSystemProperties.getWorspacePathLocation(true)).isEqualTo("-Ddesigner.workspace.path=\""+projectPath.toFile().toURI()+"\"");
        assertThat(workspaceSystemProperties.getWorspacePathLocation(false)).isEqualTo("-Ddesigner.workspace.path="+projectPath.toFile().toURI());
    }
}
