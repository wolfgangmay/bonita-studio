/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.connectors.ui.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.bpm.model.parameter.builders.ParameterBuilder.aParameter;
import static org.bonitasoft.bpm.model.process.builders.DataBuilder.aData;
import static org.mockito.Mockito.when;

import org.bonitasoft.bpm.model.connectorconfiguration.ConnectorConfiguration;
import org.bonitasoft.bpm.model.connectorconfiguration.ConnectorConfigurationFactory;
import org.bonitasoft.bpm.model.parameter.Parameter;
import org.bonitasoft.bpm.model.parameter.ParameterFactory;
import org.bonitasoft.bpm.model.process.Activity;
import org.bonitasoft.bpm.model.process.Connector;
import org.bonitasoft.bpm.model.process.ContractInput;
import org.bonitasoft.bpm.model.process.ContractInputType;
import org.bonitasoft.bpm.model.process.Data;
import org.bonitasoft.bpm.model.process.ProcessFactory;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.eclipse.jface.viewers.Viewer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectorAvailableExpressionTypeFilterTest {

    private ConnectorAvailableExpressionTypeFilter connectorAvailableExpressionTypeFilter;

    @Mock
    private Viewer expressionViewer;

    @BeforeEach
    void setUp() throws Exception {
        connectorAvailableExpressionTypeFilter = new ConnectorAvailableExpressionTypeFilter();
    }

    @Test
    void should_accept_default_types()
            throws Exception {
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createConstantExpression("", ""))).isTrue();
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createVariableExpression(aData().build()))).isTrue();
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createParameterExpression(aParameter().build()))).isTrue();
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createGroovyScriptExpression("", ""))).isTrue();
    }

    @Test
    void should_select_returns_true_for_contract_input_expression_in_on_finish_connector_input()
            throws Exception {
        final Activity activity = ProcessFactory.eINSTANCE.createActivity();
        final Connector onFinishConnector = ProcessFactory.eINSTANCE.createConnector();
        onFinishConnector.setEvent(ConnectorEvent.ON_FINISH.name());
        activity.getConnectors().add(onFinishConnector);
        when(expressionViewer.getInput()).thenReturn(onFinishConnector);

        final ContractInput input = ProcessFactory.eINSTANCE.createContractInput();
        input.setName("myInput");
        input.setType(ContractInputType.TEXT);
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createContractInputExpression(input))).isTrue();
    }

    @Test
    void should_select_returns_true_for_contract_input_expression_in_on_enter_connector_input() throws Exception {
        final Connector onFinishConnector = ProcessFactory.eINSTANCE.createConnector();
        onFinishConnector.setEvent(ConnectorEvent.ON_ENTER.name());
        final ConnectorConfiguration config = ConnectorConfigurationFactory.eINSTANCE.createConnectorConfiguration();
        onFinishConnector.setConfiguration(config);
        when(expressionViewer.getInput()).thenReturn(config);

        final ContractInput input = ProcessFactory.eINSTANCE.createContractInput();
        input.setName("myInput");
        input.setType(ContractInputType.TEXT);
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createContractInputExpression(input))).isFalse();
    }

    @Test
    void should_select_returns_true_for_variable_expression_in_connector_input()
            throws Exception {
        final Data data = ProcessFactory.eINSTANCE.createData();
        data.setName("myData");
        data.setDataType(ModelHelper.createStringDataType());
        assertThat(
                connectorAvailableExpressionTypeFilter.select(null, null, ExpressionHelper.createVariableExpression(data)))
                        .isTrue();
    }

    @Test
    void should_select_returns_true_for_parameter_expression() throws Exception {
        final ConnectorConfiguration config = ConnectorConfigurationFactory.eINSTANCE.createConnectorConfiguration();
        when(expressionViewer.getInput()).thenReturn(config);

        final Parameter param = ParameterFactory.eINSTANCE.createParameter();
        param.setName("myParam");
        param.setTypeClassname(String.class.getName());
        assertThat(connectorAvailableExpressionTypeFilter.select(expressionViewer, null,
                ExpressionHelper.createParameterExpression(param))).isTrue();
    }

}
