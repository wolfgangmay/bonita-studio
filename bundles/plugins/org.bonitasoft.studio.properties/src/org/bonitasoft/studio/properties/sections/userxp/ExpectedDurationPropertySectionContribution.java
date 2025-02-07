/**
 * Copyright (C) 2009-2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.studio.properties.sections.userxp;

import jakarta.inject.Inject;

import org.bonitasoft.bpm.model.process.ProcessPackage;
import org.bonitasoft.bpm.model.process.ReceiveTask;
import org.bonitasoft.bpm.model.process.SendTask;
import org.bonitasoft.bpm.model.process.Task;
import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.ui.jface.databinding.CustomEMFEditObservables;
import org.bonitasoft.studio.common.ui.properties.ExtensibleGridPropertySection;
import org.bonitasoft.studio.common.ui.properties.IExtensibleGridPropertySectionContribution;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.viewer.DefaultExpressionNameResolver;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

/**
 * @author Romain Bioteau
 *
 */
public class ExpectedDurationPropertySectionContribution implements IExtensibleGridPropertySectionContribution {


    private final TaskSelectionProvider selectionProvider;
    private EMFDataBindingContext context;

    @Inject
    public ExpectedDurationPropertySectionContribution(TaskSelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }
    
    @Override
    public boolean isRelevantFor(EObject eObject) {
        return eObject instanceof Task
                && !(eObject instanceof ReceiveTask)
                && !(eObject instanceof SendTask);
    }

    @Override
    public void refresh() {
    	
    }

    @Override
    public void setEditingDomain(TransactionalEditingDomain editingDomain) {
        
    }


    @Override
    public void createControl(Composite composite, TabbedPropertySheetWidgetFactory widgetFactory,
            final ExtensibleGridPropertySection page) {
        context = new EMFDataBindingContext();
        final ExpressionViewer viewer = new ExpressionViewer(composite, SWT.BORDER , widgetFactory);
        viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).indent(5, 0).create());
        viewer.setMessage(Messages.dueDateCalculationHint);
        viewer.setExpressionNameResolver(new DefaultExpressionNameResolver("dueDateCalculation"));
        viewer.addFilter(new AvailableExpressionTypeFilter(ExpressionConstants.CONSTANT_TYPE,ExpressionConstants.VARIABLE_TYPE,ExpressionConstants.SCRIPT_TYPE,ExpressionConstants.PARAMETER_TYPE));
        context.bindValue(ViewerProperties.input().observe(Realm.getDefault(), viewer),  ViewerProperties.singleSelection().observe(selectionProvider));
        context.bindValue(ViewerProperties.singleSelection().observe(viewer), 
                CustomEMFEditObservables.observeDetailValue(Realm.getDefault(), ViewerProperties.singleSelection().observe(selectionProvider), ProcessPackage.Literals.TASK__EXPECTED_DURATION));
    }

    @Override
    public void dispose() {
        if(context != null){
            context.dispose();
        }
    }

    @Override
    public String getLabel() {
        return  Messages.dueDateCalculation;
    }

    @Override
    public void setEObject(EObject object) {
    }

    @Override
    public void setSelection(ISelection selection) {
        selectionProvider.setSelection(selection);
    }

}
