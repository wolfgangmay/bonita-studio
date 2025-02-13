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

import static org.bonitasoft.bpm.model.process.builders.ConnectorBuilder.aConnector;
import static org.bonitasoft.bpm.model.process.builders.TaskBuilder.aTask;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.eclipse.jface.viewers.Viewer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectorEventFilterTest {

	@Mock
	private Viewer viewer;

	/**
	 * @return
	 */
	private ConnectorEventFilter createConnectorEventFilter() {
		return new ConnectorEventFilter(ConnectorEvent.ON_FINISH.toString());
	}

	@Test
	void should_return_true_when_connectorEvent_isOnFinish() {
		final ConnectorEventFilter onFinishFilter = createConnectorEventFilter();

		assertTrue(onFinishFilter.select(viewer, null,
				aConnector().onEvent(ConnectorEvent.ON_FINISH.name()).build()));
	}

	@Test
	void select_should_return_false_when_connectorEvent_isOnEnter() {
		final ConnectorEventFilter onFinishFilter = createConnectorEventFilter();

		assertFalse(onFinishFilter.select(viewer, null,
				aConnector().onEvent(ConnectorEvent.ON_ENTER.name()).build()));
	}

	@Test
	void select_shoud_return_false_when_notAConnector() {
		final ConnectorEventFilter onFinishFilter = createConnectorEventFilter();

		assertFalse(onFinishFilter.select(viewer, null, aTask().build()));
	}

	@Test
	void should_create_connectorEventFilter_when_ConnectorEvent_isOnEnter() {
		assertNotNull(new ConnectorEventFilter(
				ConnectorEvent.ON_ENTER.toString()));
	}

	@Test
	void should_failed_when_event_isNot_ON_ENTER_Or_ON_FINISH() {
	   assertThrows(IllegalArgumentException.class, () -> new ConnectorEventFilter("a"));
	}

}
