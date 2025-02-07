/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.dependencies.ui.dialog;

import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.common.repository.provider.FileStoreLabelProvider;
import org.bonitasoft.studio.dependencies.i18n.Messages;
import org.bonitasoft.studio.dependencies.repository.DependencyFileStore;
import org.bonitasoft.studio.dependencies.repository.DependencyRepositoryStore;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Aurelien Pupier
 */
public class ManageJarDialog extends Dialog {

    protected TableViewer tableViewer;
    private Button removeButton;
    private final DependencyRepositoryStore libStore;

    protected DataBindingContext context;
    private ViewerFilter searchFilter;
    private Text searchText;

    public ManageJarDialog(final Shell parentShell) {
        super(parentShell);
        libStore = RepositoryManager.getInstance().getRepositoryStore(DependencyRepositoryStore.class);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.manageJarTitle);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        context = new DataBindingContext();
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(15, 15).create());
        new Label(composite, SWT.NONE); //dummy
        createSearchText(composite);
        createTree(composite);
        return composite;
    }


    protected void createTree(final Composite composite) {
        tableViewer = new TableViewer(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(300, 300).create());
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addFilter(searchFilter);
        tableViewer.setLabelProvider(new FileStoreLabelProvider());
        tableViewer.setInput(libStore.findJarDependencies());

        tableViewer.addSelectionChangedListener(e -> {
            if (removeButton != null) {
                DependencyFileStore fStore = (DependencyFileStore) tableViewer.getStructuredSelection()
                        .getFirstElement();
                removeButton.setEnabled(!tableViewer.getSelection().isEmpty()
                        && fStore.canBeDeleted());
            }
        });

    }

    private void createSearchText(final Composite composite) {
        searchText = new Text(composite, SWT.SEARCH | SWT.ICON_SEARCH | SWT.BORDER | SWT.CANCEL);
        searchText.setMessage(Messages.search);
        searchText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

        searchFilter = new ViewerFilter() {

            @Override
            public boolean select(final Viewer arg0, final Object arg1, final Object element) {
                if (!searchText.getText().isEmpty()) {
                    final String searchQuery = searchText.getText().toLowerCase();
                    final IRepositoryFileStore file = (IRepositoryFileStore) element;
                    return file.getName().toLowerCase().contains(searchQuery);
                }
                return true;
            }
        };

        searchText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                tableViewer.refresh();
            }
        });
    }

    @Override
    public boolean close() {
        final boolean returnValue = super.close();
        if (returnValue) {
            if(tableViewer!= null && tableViewer.getTable() != null) {
                tableViewer.getTable().dispose();
            }
        }
        if (context != null) {
            context.dispose();
        }
        return returnValue;

    }

}
