/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.connectors.ui.property.section;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.connector.model.definition.migration.ConnectorConfigurationMigratorFactory;
import org.bonitasoft.studio.connector.model.definition.migration.ConnectorConfigurationToConnectorDefinitionConverter;
import org.bonitasoft.studio.connectors.ui.wizard.ConnectorWizard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectorOutSectionTest {

    @Mock
    private ConnectorConfigurationMigratorFactory connectorConfigurationFactory;
    @Mock
    private ConnectorConfigurationToConnectorDefinitionConverter configurationToDefinitionConverter;
    @Mock
    private RepositoryAccessor repositoryAccessor;

    @Test
    void should_setConnectorEvent_when_creating_aConnetorWizard() {
        final ConnectorOutSection section = new ConnectorOutSection(connectorConfigurationFactory,
                configurationToDefinitionConverter, repositoryAccessor);

        final ConnectorWizard wizard = section.createAddConnectorWizard();
        assertThat(wizard.getWorkingCopyConnector().getEvent()).isEqualTo(
                ConnectorEvent.ON_FINISH.name());
    }

    @Test
    void should_return_OnFinish_ConnectorEventFilter() {
        final ConnectorOutSection section = new ConnectorOutSection(connectorConfigurationFactory,
                configurationToDefinitionConverter, repositoryAccessor);

        assertThat(section.getViewerFilter()).isInstanceOf(
                ConnectorEventFilter.class);
        assertThat(
                ((ConnectorEventFilter) section.getViewerFilter()).getEvent())
                        .isEqualTo(ConnectorEvent.ON_FINISH.name());
    }

}
