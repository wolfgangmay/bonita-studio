/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.tests.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.document.core.repository.DocumentRepositoryStore;
import org.bonitasoft.studio.expression.editor.operation.OperatorLabelProvider;
import org.bonitasoft.studio.swtbot.framework.application.BotApplicationWorkbenchWindow;
import org.bonitasoft.studio.swtbot.framework.composite.BotOperationComposite;
import org.bonitasoft.studio.swtbot.framework.conditions.AssertionCondition;
import org.bonitasoft.studio.swtbot.framework.diagram.BotProcessDiagramPerspective;
import org.bonitasoft.studio.swtbot.framework.diagram.BotProcessDiagramPropertiesViewFolder;
import org.bonitasoft.studio.swtbot.framework.diagram.general.documents.BotAddDocumentDialog;
import org.bonitasoft.studio.swtbot.framework.diagram.general.documents.BotDocumentsPropertySection;
import org.bonitasoft.studio.swtbot.framework.diagram.general.documents.BotFileStoreSelectDialog;
import org.bonitasoft.studio.swtbot.framework.diagram.general.documents.BotRemoveDocumentDialog;
import org.bonitasoft.studio.swtbot.framework.diagram.general.operations.BotOperationsPropertySection;
import org.bonitasoft.studio.swtbot.framework.expression.BotExpressionEditorDialog;
import org.bonitasoft.studio.swtbot.framework.rule.SWTGefBotRule;
import org.bonitasoft.studio.tests.util.ProjectUtil;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestDocument {

    private final SWTGefBot bot = new SWTGefBot();

    @Rule
    public SWTGefBotRule rule = new SWTGefBotRule(bot);

    @BeforeClass
    @AfterClass
    public static void cleanRepo() throws Exception {
    	new SWTBot().sleep(500);
        ProjectUtil.cleanProject();
        new SWTBot().sleep(500);
    }

    @Test
    public void testAddEditDeleteDocument() {
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("doc1");
        botAddDocumentDialog.finish();

        //Edit
        BotAddDocumentDialog documentDialogBot = botDocumentsPropertySection.editDocument("doc1");
        documentDialogBot.setName("doc1Edited");
        documentDialogBot.chooseExternalInitialContent();
        bot.waitUntil(new AssertionCondition() {
            
            @Override
            protected void makeAssert() throws Exception {
                Assertions.assertThat(documentDialogBot.hasErrorMessageUrl()).isTrue();
            }
        });
        botAddDocumentDialog.setURLWithExpressionEditor("http://url-test");
        bot.waitUntil(new AssertionCondition() {
            
            @Override
            protected void makeAssert() throws Exception {
                Assertions.assertThat(documentDialogBot.hasErrorMessageUrl()).isFalse();
            }
        });
      

        botAddDocumentDialog.ok();

        //Delete
        final BotRemoveDocumentDialog botRemoveDocumentDialog = botDocumentsPropertySection
                .removeDocument("doc1Edited");
        botRemoveDocumentDialog.ok();
    }

    @Test
    public void testAddEditDeleteMultipleDocument() {
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("doc11");
        botAddDocumentDialog.chooseMultipleContent();
        botAddDocumentDialog.finish();

        //Edit
        botAddDocumentDialog = botDocumentsPropertySection.editDocument("doc11");
        botAddDocumentDialog.chooseScriptInitialContent();
        botAddDocumentDialog.setName("doc11Edited");
        botAddDocumentDialog.setInitialContents("anEmptyScript", "[]");
        botAddDocumentDialog.ok();

        //Delete
        final BotRemoveDocumentDialog botRemoveDocumentDialog = botDocumentsPropertySection
                .removeDocument("doc11Edited");
        botRemoveDocumentDialog.ok();
    }

    @Test
    public void testUploadFile() {
        final String fileName = "Idea.jpg";
        final DocumentRepositoryStore store = RepositoryManager.getInstance()
                .getRepositoryStore(DocumentRepositoryStore.class);
        store.importInputStream(fileName, this.getClass().getResourceAsStream(fileName));
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        final BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("idea");
        botAddDocumentDialog.chooseInternalInitialContent();
        final BotFileStoreSelectDialog botFileStoreSelectDialog = botAddDocumentDialog.browseInternalFile();
        botFileStoreSelectDialog.selectFile(fileName);
        botFileStoreSelectDialog.ok();
        assertInitialContentNotEmpty(botAddDocumentDialog);
        botAddDocumentDialog.finish();
        bot.saveAllEditors();
        store.getChild(fileName, true).delete();
    }

    @Test
    public void testErrorMessages() {
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("doc1");
        botAddDocumentDialog.finish();

        botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("doc1");
        final boolean errorMessageAlreadyExist = botAddDocumentDialog.isErrorMessageAlreadyExist("doc1");
        botAddDocumentDialog.setName("");
        final boolean errorMessageNameEmpty = botAddDocumentDialog.isErrorMessageNameEmpty();
        botAddDocumentDialog.setName("doc2");
        botAddDocumentDialog.chooseExternalInitialContent();
        final boolean isErrorMessageForURLAppeared = botAddDocumentDialog.hasErrorMessageUrl();
        botAddDocumentDialog.chooseInternalInitialContent();
        final boolean isErrorMessageFileAppeared = botAddDocumentDialog.hasErrorMessageFile();

        botAddDocumentDialog.cancel();

        assertThat(isErrorMessageForURLAppeared).isTrue();
        assertThat(errorMessageAlreadyExist).isTrue();
        assertThat(errorMessageNameEmpty).isTrue();
        assertThat(isErrorMessageFileAppeared).isTrue();
    }

    @Test
    public void testErrorMessagesBehavior() {
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        final BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();

        // Open Dialog
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        Assertions.assertThat(botAddDocumentDialog.isFinishEnabled()).isFalse();
        Assertions.assertThat(botAddDocumentDialog.isFinishAndAddEnabled()).isFalse();

        // internal Content
        botAddDocumentDialog.chooseInternalInitialContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.isErrorMessageNameEmpty());

        // Name
        botAddDocumentDialog.setName("document1");
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageFile());

        // None
        botAddDocumentDialog.chooseNoneInitialContent();
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        assertThat(botAddDocumentDialog.canFinish()).isTrue();

        // External Content
        botAddDocumentDialog.chooseExternalInitialContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageUrl());
        botAddDocumentDialog.setURL("http://internet.com/logo.jpg");
        bot.sleep(200); // wait the 500ms delay
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        assertThat(botAddDocumentDialog.canFinish()).isTrue();

        // Internal Content
        botAddDocumentDialog.chooseInternalInitialContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageFile());
        botAddDocumentDialog.setFile("toto.txt");
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        assertThat(botAddDocumentDialog.canFinish()).isTrue();

        botAddDocumentDialog.finish();
    }

    private void assertErrorMessageAndFinishDisabled(final BotAddDocumentDialog botAddDocumentDialog,
            final boolean errorMessageShowed) {
        Assertions.assertThat(errorMessageShowed).isTrue();
        Assertions.assertThat(botAddDocumentDialog.isFinishEnabled()).isFalse();
        Assertions.assertThat(botAddDocumentDialog.isFinishAndAddEnabled()).isFalse();
    }

    private void assertInitialContentNotEmpty(final BotAddDocumentDialog botAddDocumentDialog) {
        Assertions.assertThat(botAddDocumentDialog.isInitialContentEmpty()).isFalse();
    }

    @Test
    public void testDocumentOperationSwitch() {
        final BotApplicationWorkbenchWindow botApplicationWorkbenchWindow = new BotApplicationWorkbenchWindow(bot);
        final BotProcessDiagramPerspective botProcessDiagramPerspective = botApplicationWorkbenchWindow
                .createNewDiagram();
        final BotProcessDiagramPropertiesViewFolder botProcessDiagramPropertiesViewFolder = botProcessDiagramPerspective
                .getDiagramPropertiesPart();
        final BotDocumentsPropertySection botDocumentsPropertySection = botProcessDiagramPropertiesViewFolder
                .selectDataTab()
                .selectDocumentsTab();
        final BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();
        botAddDocumentDialog.setName("doc1");
        botAddDocumentDialog.finish();

        botProcessDiagramPerspective.activeProcessDiagramEditor().selectElement("Step1");

        final BotOperationsPropertySection botOperationsPropertySection = botProcessDiagramPropertiesViewFolder
                .selectExecutionTab().selectOperationTab();
        botOperationsPropertySection.addOperation();
        final BotOperationComposite botOperationComposite = botOperationsPropertySection.getOperation(0);
        botOperationComposite.selectLeftOperand("doc1", String.class.getName());
        final String expectedOperator = new OperatorLabelProvider().getText(ExpressionConstants.SET_DOCUMENT_OPERATOR);
        Assertions.assertThat(botOperationComposite.getSelectedOperator()).isEqualTo(expectedOperator);

        final BotExpressionEditorDialog editRightOperand = botOperationComposite.editRightOperand();
        Assertions.assertThat(editRightOperand.selectScriptTab().getReturnType())
                .isEqualTo(ExpressionConstants.DOCUMENT_VALUE_RETURN_TYPE);

        editRightOperand.cancel();

    }

    protected BotDocumentsPropertySection createDiagramAndGoToDocumentSection() {
        final BotApplicationWorkbenchWindow botApplicationWorkbenchWindow = new BotApplicationWorkbenchWindow(bot);
        final BotProcessDiagramPerspective botProcessDiagramPerspective = botApplicationWorkbenchWindow
                .createNewDiagram();
        final BotProcessDiagramPropertiesViewFolder botProcessDiagramPropertiesViewFolder = botProcessDiagramPerspective
                .getDiagramPropertiesPart();
        final BotDocumentsPropertySection botDocumentsPropertySection = botProcessDiagramPropertiesViewFolder
                .selectDataTab()
                .selectDocumentsTab();
        return botDocumentsPropertySection;
    }

    @Test
    public void testErrorMessageAndButtonBehaviorForMultipleDocument() throws Exception {
        final BotDocumentsPropertySection botDocumentsPropertySection = createDiagramAndGoToDocumentSection();
        final BotAddDocumentDialog botAddDocumentDialog = botDocumentsPropertySection.addDocument();

        botAddDocumentDialog.chooseMultipleContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.isErrorMessageNameEmpty());

        botAddDocumentDialog.chooseSingleContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.isErrorMessageNameEmpty());

        botAddDocumentDialog.setName("newDoc1");
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isFalse();

        // INTERNAL
        botAddDocumentDialog.chooseInternalInitialContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageFile());
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isTrue();

        botAddDocumentDialog.chooseMultipleContent();
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isFalse();

        botAddDocumentDialog.chooseSingleContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageFile());
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isTrue();

        // EXTERNAL
        botAddDocumentDialog.chooseExternalInitialContent();;
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageUrl());
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isTrue();

        botAddDocumentDialog.chooseMultipleContent();
        assertThat(botAddDocumentDialog.hasNoValidationError()).isTrue();
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isFalse();

        botAddDocumentDialog.chooseSingleContent();
        assertErrorMessageAndFinishDisabled(botAddDocumentDialog, botAddDocumentDialog.hasErrorMessageUrl());
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isTrue();

        // NONE
        botAddDocumentDialog.chooseNoneInitialContent();
        Assertions.assertThat(botAddDocumentDialog.isMymeTypeFieldEnabled()).isFalse();
        botAddDocumentDialog.finish();

    }

}
