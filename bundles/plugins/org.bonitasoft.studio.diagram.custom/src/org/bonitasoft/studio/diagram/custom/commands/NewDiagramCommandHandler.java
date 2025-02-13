/**
 * Copyright (C) 2009-2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.diagram.custom.commands;

import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.diagram.custom.parts.CustomPoolEditPart;
import org.bonitasoft.studio.diagram.custom.repository.DiagramFileStore;
import org.bonitasoft.studio.diagram.custom.repository.NewDiagramFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class NewDiagramCommandHandler extends AbstractHandler {

    @Override
    public DiagramFileStore execute(final ExecutionEvent event) throws ExecutionException {
        final DiagramFileStore diagramFileStore = newDiagram();
        Display.getDefault().asyncExec(() -> {
                final IEditorPart editor = (IEditorPart) diagramFileStore.open();
                if (editor instanceof DiagramEditor) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(editor);
                }
        });
        return diagramFileStore;
    }

    public DiagramFileStore newDiagram() {
        final NewDiagramFactory diagramFactory = new NewDiagramFactory(RepositoryManager.getInstance().getCurrentRepository().orElseThrow());
        diagramFactory.setDefaultPoolWidth(CustomPoolEditPart.getDefaultWidth());
        final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        try {
            progressService.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    diagramFactory.create(monitor);
                    monitor.done();
                }
            });
        } catch (final InvocationTargetException e) {
            BonitaStudioLog.error(e);
        } catch (final InterruptedException e) {
            BonitaStudioLog.error(e);
        }
        final DiagramFileStore diagramFileStore = diagramFactory.getNewDiagramFileStore();
        return diagramFileStore;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
