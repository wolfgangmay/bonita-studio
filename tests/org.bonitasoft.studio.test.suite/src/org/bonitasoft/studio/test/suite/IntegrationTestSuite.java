/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.test.suite;

import org.bonitasoft.studio.tests.BonitaProjectIT;
import org.bonitasoft.studio.tests.TestFullScenario;
import org.bonitasoft.studio.tests.TestPatchedBundles;
import org.bonitasoft.studio.tests.TestPathSize;
import org.bonitasoft.studio.tests.bar.ExportBarIT;
import org.bonitasoft.studio.tests.bar.TestExportProcessBar;
import org.bonitasoft.studio.tests.businessobject.DeployBDMOperationIT;
import org.bonitasoft.studio.tests.businessobject.ImportLegacyBDMIT;
import org.bonitasoft.studio.tests.conditions.TestConditions;
import org.bonitasoft.studio.tests.configuration.TestConfigurationSynhronizer;
import org.bonitasoft.studio.tests.connectors.DatabaseDriverConfigurationIT;
import org.bonitasoft.studio.tests.connectors.GroovyConnectorIT;
import org.bonitasoft.studio.tests.connectors.TestConnectorOperationIT;
import org.bonitasoft.studio.tests.connectors.TestWebserviceVersionForBPMNImport;
import org.bonitasoft.studio.tests.data.DataRefactorIT;
import org.bonitasoft.studio.tests.deploy.TestDeployCommand;
import org.bonitasoft.studio.tests.designer.UIDArtifactCreationIT;
import org.bonitasoft.studio.tests.document.RefactorDocumentOperationTest;
import org.bonitasoft.studio.tests.document.TestDocumentRefactoring;
import org.bonitasoft.studio.tests.engine.RuntimeIntegrationIT;
import org.bonitasoft.studio.tests.engine.TestSubprocessEventExport;
import org.bonitasoft.studio.tests.exporter.bpmn.BPMNDataExportImportTest;
import org.bonitasoft.studio.tests.exporter.bpmn.BPMNImportExportTest;
import org.bonitasoft.studio.tests.exporter.bpmn.BPMNSequenceFlowConditionExportImportTest;
import org.bonitasoft.studio.tests.importer.api.ProcBuilderTests;
import org.bonitasoft.studio.tests.importer.bos.ImportBOSArchiveIT;
import org.bonitasoft.studio.tests.importer.bpmn2.TestImportBPMN2;
import org.bonitasoft.studio.tests.migration.DocumentMigrationIT;
import org.bonitasoft.studio.tests.organization.ImportOrganizationIT;
import org.bonitasoft.studio.tests.organization.TestExportOrganization;
import org.bonitasoft.studio.tests.parameter.TestParametersRefactoring;
import org.bonitasoft.studio.tests.parameter.TestProcessParameters;
import org.bonitasoft.studio.tests.properties.TestMessageRefactoring;
import org.bonitasoft.studio.tests.repository.TestImportExportAndDeleteRepository;
import org.bonitasoft.studio.tests.repository.TestImportRepository;
import org.bonitasoft.studio.tests.repository.UIDesignerWorkspaceIntegrationIT;
import org.bonitasoft.studio.tests.restApiExtension.BuildAndDeployRestAPIExtensionIT;
import org.bonitasoft.studio.tests.restApiExtension.CreateRestAPIExtensionProjectIT;
import org.bonitasoft.studio.tests.restApiExtension.ExportRestAPIExtensionProjectIT;
import org.bonitasoft.studio.tests.restApiExtension.RestAPIExtensionMarkerResolutionIT;
import org.bonitasoft.studio.tests.searchindex.TestRunSearchIndex;
import org.bonitasoft.studio.tests.searchindex.TestSearchIndexRefactoring;
import org.bonitasoft.studio.tests.subprocess.TestSubprocess;
import org.bonitasoft.studio.tests.timer.TestNonInterruptingBoundaryTimerEvent;
import org.bonitasoft.studio.tests.util.BonitaSuite;
import org.bonitasoft.studio.tests.validation.TestTokenDispatcher;
import org.bonitasoft.studio.tests.validation.TestValidationConstraints;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(BonitaSuite.class)
@Suite.SuiteClasses({
        BonitaProjectIT.class,
        TestPatchedBundles.class,
        TestFullScenario.class,
        TestSubprocess.class,
        TestRunSearchIndex.class,
        TestSearchIndexRefactoring.class,
        TestConditions.class,
        TestPathSize.class,
        ImportOrganizationIT.class,
        TestExportOrganization.class,
        TestDocumentRefactoring.class,
        RefactorDocumentOperationTest.class,
        TestConfigurationSynhronizer.class,
        TestImportExportAndDeleteRepository.class,
        ProcBuilderTests.class,
        TestImportRepository.class,
        TestImportBPMN2.class,
        TestSubprocessEventExport.class,
        DatabaseDriverConfigurationIT.class,
        ImportBOSArchiveIT.class,
        TestExportProcessBar.class,
        BPMNImportExportTest.class,
        BPMNDataExportImportTest.class,
        BPMNSequenceFlowConditionExportImportTest.class,
        DataRefactorIT.class,
        TestNonInterruptingBoundaryTimerEvent.class,
        TestDeployCommand.class,
        TestValidationConstraints.class,
        TestConnectorOperationIT.class,
        TestWebserviceVersionForBPMNImport.class,
        TestTokenDispatcher.class,
        DocumentMigrationIT.class,
        GroovyConnectorIT.class,
        DeployBDMOperationIT.class,
        ImportLegacyBDMIT.class,
        TestProcessParameters.class,
        TestParametersRefactoring.class,
        ExportBarIT.class,
        UIDesignerWorkspaceIntegrationIT.class,
        TestMessageRefactoring.class,
        UIDArtifactCreationIT.class,
        CreateRestAPIExtensionProjectIT.class,
        RestAPIExtensionMarkerResolutionIT.class,
        BuildAndDeployRestAPIExtensionIT.class,
        ExportRestAPIExtensionProjectIT.class,
        RuntimeIntegrationIT.class
})
public class IntegrationTestSuite {

}
