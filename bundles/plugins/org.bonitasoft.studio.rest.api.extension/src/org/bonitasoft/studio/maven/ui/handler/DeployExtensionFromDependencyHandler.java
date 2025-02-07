/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.studio.maven.ui.handler;

import java.lang.reflect.InvocationTargetException;

import jakarta.inject.Named;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.core.maven.model.GAV;
import org.bonitasoft.studio.common.ui.IDisplayable;
import org.bonitasoft.studio.common.ui.jface.BonitaErrorDialog;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.bonitasoft.studio.engine.http.HttpClientFactory;
import org.bonitasoft.studio.engine.operation.GetApiSessionOperation;
import org.bonitasoft.studio.maven.ExtensionProjectFileStore;
import org.bonitasoft.studio.maven.ExtensionRepositoryStore;
import org.bonitasoft.studio.maven.i18n.Messages;
import org.bonitasoft.studio.maven.operation.DeployCustomPageProjectOperation;
import org.bonitasoft.studio.rest.api.extension.RestAPIExtensionActivator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DeployExtensionFromDependencyHandler {

    @Execute
    public void execute(RepositoryAccessor repositoryAccessor,
            HttpClientFactory httpClientFactory,
            @Named("groupId") String groupId,
            @Named("artifactId") String artifactId,
            @Named("version") String version,
            @Named("classifier") @Optional String classifier) {
        var store = repositoryAccessor.getRepositoryStore(ExtensionRepositoryStore.class);
        GAV gav = new GAV(groupId, artifactId, version, classifier, "zip", null);
        store.findByGAV(gav)
            .ifPresent(fStore -> deploy(fStore, httpClientFactory));
    }

    protected boolean deploy(ExtensionProjectFileStore fileStore, HttpClientFactory httpClientFactory) {
        String displayName = IDisplayable.toDisplayName(fileStore).orElse("");
        GetApiSessionOperation apiSessionOperation = new GetApiSessionOperation();
        try {
            APISession apiSession = apiSessionOperation.execute();
            var operation = new DeployCustomPageProjectOperation(BOSEngineManager.getInstance().getPageAPI(apiSession),
                    httpClientFactory,
                    fileStore);
            PlatformUI.getWorkbench().getProgressService().run(true, false, operation::run);
            IStatus status = operation.getStatus();
            if (!status.isOK()) {
                return showDeployErrorDialog(status);
            }
        } catch (InvocationTargetException | InterruptedException | BonitaHomeNotSetException | ServerAPIException
                | UnknownAPITypeException e) {
            new BonitaErrorDialog(Display.getDefault().getActiveShell(), Messages.errorTitle,
                    NLS.bind(Messages.deployFailedMessage, displayName), e).open();
            BonitaStudioLog.error(e);
            return false;
        } finally {
            apiSessionOperation.logout();
        }
        return openDeploySuccessDialog(displayName);
    }

    protected boolean openDeploySuccessDialog(final String restApiName) {
        MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.deploySuccessTitle,
                NLS.bind(Messages.deploySuccessMessage, restApiName));
        return true;
    }

    protected boolean showDeployErrorDialog(final IStatus status) {
        BonitaStudioLog.error("Failed to deploy custom page project.", status.getException(),
                RestAPIExtensionActivator.PLUGIN_ID);
        new BonitaErrorDialog(Display.getDefault().getActiveShell(), Messages.errorTitle, status.getMessage(), status,
                IStatus.ERROR).open();
        return false;
    }
}
