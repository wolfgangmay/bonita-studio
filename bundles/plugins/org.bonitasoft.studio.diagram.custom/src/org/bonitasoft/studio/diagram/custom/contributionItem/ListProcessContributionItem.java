/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.studio.diagram.custom.contributionItem;

import java.util.Optional;
import java.util.stream.Collectors;

import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.filestore.FileStoreFinder;
import org.bonitasoft.studio.diagram.custom.repository.DiagramFileStore;
import org.bonitasoft.bpm.model.process.AbstractProcess;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;

public abstract class ListProcessContributionItem extends CompoundContributionItem {

    protected FileStoreFinder fileStoreFinder;
    protected RepositoryAccessor repositoryAccessor;
    protected ICommandService iCommandService;

    public ListProcessContributionItem() {
        fileStoreFinder = new FileStoreFinder();
        repositoryAccessor = RepositoryManager.getInstance().getAccessor();
        iCommandService = PlatformUI.getWorkbench().getService(ICommandService.class);
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        Optional<DiagramFileStore> fileStore = fileStoreFinder
                .findSelectedFileStore(repositoryAccessor.getCurrentRepository().orElseThrow())
                .filter(DiagramFileStore.class::isInstance)
                .map(DiagramFileStore.class::cast);
        if (fileStore.isPresent()) {
            return fileStore.get().getProcesses(true).stream()
                    .map(this::createContributionItem)
                    .collect(Collectors.toList()).toArray(new IContributionItem[0]);
        }
        return new IContributionItem[0];
    }

    private IContributionItem createContributionItem(AbstractProcess process) {
        return new ContributionItem() {

            @Override
            public void fill(Menu menu, int index) {
                MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText(String.format("%s (%s)", process.getName(), process.getVersion()));
                item.addListener(SWT.Selection, createSelectionListener(process));
            }
        };
    }

    protected abstract Listener createSelectionListener(AbstractProcess process);
}
