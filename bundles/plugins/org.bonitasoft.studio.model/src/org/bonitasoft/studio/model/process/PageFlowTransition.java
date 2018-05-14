/**
 * Copyright (C) 2009-2015 BonitaSoft S.A.
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
package org.bonitasoft.studio.model.process;

import org.bonitasoft.studio.model.expression.Expression;

import org.bonitasoft.studio.model.form.Form;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Page Flow Transition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.bonitasoft.studio.model.process.PageFlowTransition#getFrom <em>From</em>}</li>
 * <li>{@link org.bonitasoft.studio.model.process.PageFlowTransition#getTo <em>To</em>}</li>
 * <li>{@link org.bonitasoft.studio.model.process.PageFlowTransition#getCondition <em>Condition</em>}</li>
 * </ul>
 *
 * @see org.bonitasoft.studio.model.process.ProcessPackage#getPageFlowTransition()
 * @model
 * @generated
 */
public interface PageFlowTransition extends EObject {

    /**
     * Returns the value of the '<em><b>From</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>From</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>From</em>' reference.
     * @see #setFrom(Form)
     * @see org.bonitasoft.studio.model.process.ProcessPackage#getPageFlowTransition_From()
     * @model
     * @generated
     */
    Form getFrom();

    /**
     * Sets the value of the '{@link org.bonitasoft.studio.model.process.PageFlowTransition#getFrom <em>From</em>}'
     * reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>From</em>' reference.
     * @see #getFrom()
     * @generated
     */
    void setFrom(Form value);

    /**
     * Returns the value of the '<em><b>To</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>To</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>To</em>' reference.
     * @see #setTo(Form)
     * @see org.bonitasoft.studio.model.process.ProcessPackage#getPageFlowTransition_To()
     * @model
     * @generated
     */
    Form getTo();

    /**
     * Sets the value of the '{@link org.bonitasoft.studio.model.process.PageFlowTransition#getTo <em>To</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>To</em>' reference.
     * @see #getTo()
     * @generated
     */
    void setTo(Form value);

    /**
     * Returns the value of the '<em><b>Condition</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Condition</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Condition</em>' containment reference.
     * @see #setCondition(Expression)
     * @see org.bonitasoft.studio.model.process.ProcessPackage#getPageFlowTransition_Condition()
     * @model containment="true"
     * @generated
     */
    Expression getCondition();

    /**
     * Sets the value of the '{@link org.bonitasoft.studio.model.process.PageFlowTransition#getCondition <em>Condition</em>}'
     * containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Condition</em>' containment reference.
     * @see #getCondition()
     * @generated
     */
    void setCondition(Expression value);

} // PageFlowTransition
