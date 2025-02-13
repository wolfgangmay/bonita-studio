/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.contract.core.mapping.operation;

import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.bpm.model.expression.Expression;
import org.bonitasoft.bpm.model.expression.ExpressionFactory;
import org.bonitasoft.bpm.model.expression.Operation;
import org.bonitasoft.bpm.model.expression.Operator;
import org.bonitasoft.bpm.model.process.ContractInput;
import org.bonitasoft.bpm.model.process.Document;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class DocumentUpdateOperationBuilder {

    private ContractInput input;
    private Document document;

    public DocumentUpdateOperationBuilder(ContractInput input, Document document) {
        this.input = input;
        this.document = document;
    }

    public Operation toOperation() {
        Operation operation = ExpressionFactory.eINSTANCE.createOperation();
        Expression rightOperand = ExpressionHelper.createExpressionFromEObject(input);
        Expression leftOperand = ExpressionHelper.createDocumentReferenceExpression(document);
        Operator operator = ExpressionFactory.eINSTANCE.createOperator();
        if (document.isMultiple()) {
            operator.setType(ExpressionConstants.SET_LIST_DOCUMENT_OPERATOR);
        } else {
            operator.setType(ExpressionConstants.SET_DOCUMENT_OPERATOR);
        }
        operation.setLeftOperand(leftOperand);
        operation.setRightOperand(rightOperand);
        operation.setOperator(operator);
        return operation;
    }

    private String format(String initialValue) {
        final org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(initialValue);
        try {
            new DefaultGroovyFormatter(document, new DefaultFormatterPreferences(), 0).format().apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
            BonitaStudioLog.error("Failed to format generated script", e);
        }
        return document.get();
    }
}
