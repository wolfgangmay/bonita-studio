/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.studio.businessobject.editor.editor.ui.formpage.index;

import static org.bonitasoft.studio.ui.databinding.UpdateStrategyFactory.neverUpdateValueStrategy;
import static org.bonitasoft.studio.ui.databinding.UpdateStrategyFactory.updateValueStrategy;

import org.bonitasoft.studio.businessobject.editor.editor.ui.control.businessObject.ReadOnlyBusinessObjectList;
import org.bonitasoft.studio.businessobject.editor.editor.ui.control.index.IndexControl;
import org.bonitasoft.studio.businessobject.editor.model.BusinessObject;
import org.bonitasoft.studio.businessobject.i18n.Messages;
import org.bonitasoft.studio.ui.databinding.ComputedValueBuilder;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;

public class IndexFormPart extends AbstractFormPart {

    private DataBindingContext ctx = new DataBindingContext();
    private IndexFormPage formPage;
    private ReadOnlyBusinessObjectList businessObjectList;
    private IndexControl indexControl;

    public IndexFormPart(Composite parent, IndexFormPage formPage) {
        this.formPage = formPage;
        parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        parent.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 600).create());

        createBusinessObjectList(parent);
        createIndexEditionControl(parent);
    }

    private void createIndexEditionControl(Composite parent) {
        indexControl = new IndexControl(parent, formPage, ctx);

        ctx.bindValue(formPage.observeBusinessObjectSelected(), indexControl.observeSectionTitle(),
                updateValueStrategy().withConverter(IConverter.<BusinessObject, String> create(
                                bo -> bo == null ? "" : String.format(Messages.indexSectionTitle, bo.getSimpleName()))).create(),
                neverUpdateValueStrategy().create());

        ctx.bindValue(indexControl.observeSectionVisible(), new ComputedValueBuilder<Boolean>()
                .withSupplier(() -> formPage.observeBusinessObjectSelected().getValue() != null)
                .build());

    }

    private void createBusinessObjectList(Composite parent) {
        Composite businessObjectListComposite = formPage.getToolkit().createComposite(parent);
        businessObjectListComposite.setLayout(GridLayoutFactory.fillDefaults().create());
        businessObjectListComposite
                .setLayoutData(GridDataFactory.fillDefaults().grab(false, true).hint(300, SWT.DEFAULT).create());

        businessObjectList = new ReadOnlyBusinessObjectList(businessObjectListComposite, formPage, ctx);
        ctx.bindValue(businessObjectList.observeInput(), formPage.observeWorkingCopy());
        businessObjectList.expandAll();
    }

    public void refreshIndexList() {
        indexControl.refreshIndexList();
    }

    public void refreshBusinessObjectList() {
        businessObjectList.refreshViewer();
    }

    public void showBusinessObjectSelection() {
        businessObjectList.showBusinessObjectSelection();
    }

}
