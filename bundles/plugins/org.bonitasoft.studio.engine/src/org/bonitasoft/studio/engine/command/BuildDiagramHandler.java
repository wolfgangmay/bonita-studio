/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.engine.command;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.inject.Named;

import org.bonitasoft.bpm.model.process.Pool;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.configuration.ConfigurationPlugin;
import org.bonitasoft.studio.configuration.preferences.ConfigurationPreferenceConstants;
import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
import org.bonitasoft.studio.engine.operation.ExportBarOperation;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class BuildDiagramHandler {

    class BuildDiagramRunnable implements Runnable {

        private IStatus status = ValidationStatus.ok();
        private String fileName;
        private String destinationPath;
        private RepositoryAccessor repositoryAccessor;
        private String processUUID;

        BuildDiagramRunnable(RepositoryAccessor repositoryAccessor, String fileName, String destinationPath,
                String processUUID) {
            this.repositoryAccessor = repositoryAccessor;
            this.fileName = fileName;
            this.destinationPath = destinationPath;
            this.processUUID = processUUID;
        }

        @Override
        public void run() {
            List<Pool> processes = retrieveProcesses(repositoryAccessor, fileName, processUUID);
            ExportBarOperation exportBarOperation = getExportOperation();
            processes.forEach(exportBarOperation::addProcessToDeploy);
            exportBarOperation.setTargetFolder(destinationPath);
            exportBarOperation.setConfigurationId(ConfigurationPlugin.getDefault().getPreferenceStore()
                    .getString(ConfigurationPreferenceConstants.DEFAULT_CONFIGURATION));
            exportBarOperation.run(new NullProgressMonitor());
            if (Objects.equals(exportBarOperation.getStatus().getSeverity(), ValidationStatus.ERROR)) {
                this.status = exportBarOperation.getStatus();
            }
        }

        public IStatus getStatus() {
            return status;
        }

    }

    @Execute
    public IStatus execute(RepositoryAccessor repositoryAccessor,
            @Named("fileName") String fileName,
            @Named("destinationPath") String destinationPath,
            @Named("process") String processUUID) {
        DiagramRepositoryStore diagramRepositoryStore = repositoryAccessor
                .getRepositoryStore(DiagramRepositoryStore.class);
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, false, diagramRepositoryStore::computeProcesses);
        } catch (InvocationTargetException | InterruptedException e) {
            BonitaStudioLog.error(e);
        }
        BuildDiagramRunnable runnable = new BuildDiagramRunnable(repositoryAccessor, fileName, destinationPath,
                processUUID);
        Display.getDefault().syncExec(runnable);
        diagramRepositoryStore.resetComputedProcesses();
        return runnable.getStatus();
    }

    private List<Pool> retrieveProcesses(RepositoryAccessor repositoryAccessor, String fileName,
            String processUUID) {
        return repositoryAccessor.getRepositoryStore(DiagramRepositoryStore.class)
                .getChild(fileName, true)
                .getProcesses(true).stream().filter(pool -> {
                    if (processUUID != null) {
                        return Objects.equals(processUUID, ModelHelper.getEObjectID(pool));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    protected ExportBarOperation getExportOperation() {
        return new ExportBarOperation();
    }

}
