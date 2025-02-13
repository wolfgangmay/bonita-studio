/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.studio.identity.organization.editor.formpage.group;

import static org.bonitasoft.studio.ui.databinding.UpdateStrategyFactory.neverUpdateValueStrategy;
import static org.bonitasoft.studio.ui.databinding.UpdateStrategyFactory.updateValueStrategy;

import org.bonitasoft.studio.identity.organization.editor.control.group.GroupEditionControl;
import org.bonitasoft.studio.identity.organization.editor.control.group.GroupList;
import org.bonitasoft.studio.identity.organization.model.organization.Group;
import org.bonitasoft.studio.ui.databinding.ComputedValueBuilder;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;

public class GroupFormPart extends AbstractFormPart {

    private DataBindingContext ctx = new DataBindingContext();
    private GroupFormPage formPage;
    private GroupList groupList;
    private GroupEditionControl groupEditionControl;

    public GroupFormPart(Composite parent, GroupFormPage formPage) {
        this.formPage = formPage;

        parent.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(2)
                .spacing(20, LayoutConstants.getSpacing().y).create());
        parent.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 600).create());

        createGroupList(parent);
        createGroupDetailsControl(parent);
    }

    @Override
    public void commit(boolean onSave) {
        if (!onSave) { // onSave, commit is performed only once, on the overviewFormPart
            formPage.commit();
        }
        super.commit(onSave);
        if (onSave) {
            getManagedForm().dirtyStateChanged();
        }
    }

    private void createGroupDetailsControl(Composite parent) {
        IObservableValue<Group> selectedGroupObservable = groupList.observeGroupSelected();
        groupEditionControl = new GroupEditionControl(parent, formPage, selectedGroupObservable, ctx);

        ctx.bindValue(groupList.observeGroupSelected(), groupEditionControl.observeSectionTitle(),
                updateValueStrategy().withConverter(IConverter.<Group, String> create(grp -> grp == null ? "" : grp.getDisplayName())).create(),
                neverUpdateValueStrategy().create());

        ctx.bindValue(groupEditionControl.observeSectionVisible(), new ComputedValueBuilder<Boolean>()
                .withSupplier(() -> selectedGroupObservable.getValue() != null)
                .build());
    }

    private void createGroupList(Composite parent) {
        Composite groupListComposite = formPage.getToolkit().createComposite(parent);
        groupListComposite.setLayout(GridLayoutFactory.fillDefaults().create());
        groupListComposite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());

        groupList = new GroupList(groupListComposite, formPage, ctx);
        groupList.expandAll();
    }

    public void refreshSelectedGroup() {
        groupList.refreshSelectedGroup();
    }

    @Override
    public void dispose() {
        groupList.dispose();
        super.dispose();
    }

    public void refreshGroupList() {
        groupList.refreshGroupList();
    }

}
