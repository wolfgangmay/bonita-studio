/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.identity.organization.handler;

import static org.bonitasoft.studio.ui.wizard.WizardPageBuilder.newPage;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import jakarta.inject.Named;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.core.ActiveOrganizationProvider;
import org.bonitasoft.studio.common.repository.filestore.AbstractFileStore;
import org.bonitasoft.studio.common.repository.model.ReadFileStoreException;
import org.bonitasoft.studio.common.ui.IDisplayable;
import org.bonitasoft.studio.identity.IdentityPlugin;
import org.bonitasoft.studio.identity.i18n.Messages;
import org.bonitasoft.studio.identity.organization.exception.OrganizationValidationException;
import org.bonitasoft.studio.identity.organization.model.organization.Organization;
import org.bonitasoft.studio.identity.organization.repository.OrganizationFileStore;
import org.bonitasoft.studio.identity.organization.repository.OrganizationRepositoryStore;
import org.bonitasoft.studio.identity.organization.ui.control.DeployOrganizationControlSupplier;
import org.bonitasoft.studio.identity.organization.validator.OrganizationValidator;
import org.bonitasoft.studio.ui.dialog.ExceptionDialogHandler;
import org.bonitasoft.studio.ui.dialog.MultiStatusDialog;
import org.bonitasoft.studio.ui.wizard.WizardBuilder;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class DeployOrganizationHandler {

    public static final String DEPLOY_ORGA_PARAMETER_NAME = "organization";

    @Execute
    public void execute(ExceptionDialogHandler exceptionDialogHandler,
            RepositoryAccessor repositoryAccessor,
            ActiveOrganizationProvider activeOrganizationProvider,
            @org.eclipse.e4.core.di.annotations.Optional @Named(DEPLOY_ORGA_PARAMETER_NAME) String organization) {

        Optional<Organization> orgaToDeploy = findOrganizationToDeploy(repositoryAccessor, organization);
        DeployOrganizationControlSupplier controlSupplier = createDeployOrganizationControlSupplier(repositoryAccessor,
                activeOrganizationProvider, orgaToDeploy);

        WizardBuilder.<IStatus> newWizard()
                .withTitle(Messages.deployOrganizationTitle)
                .needProgress()
                .havingPage(newPage()
                        .withTitle(Messages.deployOrganizationPageTitle)
                        .withDescription(Messages.deployOrganizationDesc)
                        .withControl(controlSupplier))
                .onFinish(wizardContainer -> deploy(wizardContainer, activeOrganizationProvider, controlSupplier))
                .open(Display.getDefault().getActiveShell(), Messages.deploy)
                .ifPresent(status -> openResultDialog(exceptionDialogHandler, controlSupplier, status));
    }

    private Optional<Organization> findOrganizationToDeploy(RepositoryAccessor repositoryAccessor,
            String organization) {
        return Optional.ofNullable(organization)
                .map(orga -> repositoryAccessor.getRepositoryStore(OrganizationRepositoryStore.class).getChild(orga,
                        true))
                .map(t -> {
                    try {
                        return t.getContent();
                    } catch (ReadFileStoreException e) {
                        BonitaStudioLog.warning(e.getMessage(), IdentityPlugin.PLUGIN_ID);
                        return null;
                    }
                });
    }

    protected void openResultDialog(ExceptionDialogHandler exceptionDialogHandler,
            DeployOrganizationControlSupplier controlSupplier, IStatus status) {
        if (status.isOK()) {
            final String organizationName = IDisplayable.toDisplayName(controlSupplier).orElse("");
            MessageDialog.openInformation(Display.getDefault().getActiveShell(),
                    Messages.deployInformationTitle,
                    NLS.bind(getSuccessMessage(), organizationName));
        } else {
            exceptionDialogHandler.openErrorDialog(Display.getDefault().getActiveShell(), status.getMessage(),
                    status.getException());
        }
    }

    protected String getSuccessMessage() {
        return Messages.deployOrganizationSuccessMsg;
    }

    protected DeployOrganizationControlSupplier createDeployOrganizationControlSupplier(
            RepositoryAccessor repositoryAccessor, ActiveOrganizationProvider activeOrganizationProvider,
            Optional<Organization> orgaToDeploy) {
        return new DeployOrganizationControlSupplier(
                activeOrganizationProvider.getDefaultUser(),
                repositoryAccessor.getRepositoryStore(OrganizationRepositoryStore.class),
                orgaToDeploy);
    }

    protected Optional<IStatus> deploy(IWizardContainer wizardContainer,
            ActiveOrganizationProvider activeOrganizationProvider, DeployOrganizationControlSupplier controlSupplier) {
        updateDefaultUserPreference(activeOrganizationProvider, controlSupplier);
        try {
            OrganizationFileStore fileStore = controlSupplier.getFileStore();
            wizardContainer.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.validatingOrganizationContent, IProgressMonitor.UNKNOWN);
                    IStatus status;
                    try {
                        status = new OrganizationValidator().validate(fileStore.getContent());
                    } catch (ReadFileStoreException e1) {
                        status = ValidationStatus.error(e1.getMessage());
                    }
                    if (!status.isOK()) {
                        throw new InvocationTargetException(new OrganizationValidationException((MultiStatus) status,
                                IDisplayable.toDisplayName(fileStore).orElse("")));
                    }
                    monitor.beginTask(Messages.deployOrganization, IProgressMonitor.UNKNOWN);
                    final ICommandService service = PlatformUI.getWorkbench()
                            .getService(ICommandService.class);
                    final IHandlerService handlerService = PlatformUI.getWorkbench()
                            .getService(IHandlerService.class);
                    final Command cmd = service.getCommand("org.bonitasoft.studio.engine.installOrganization");
                    try {
                        final Parameterization p = new Parameterization(cmd.getParameter("artifact"),
                                fileStore.getName());
                        handlerService.executeCommand(new ParameterizedCommand(cmd, new Parameterization[] { p }),
                                null);
                        AbstractFileStore.refreshExplorerView();
                    } catch (final Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (final InvocationTargetException | InterruptedException e) {
            if (e.getCause() instanceof OrganizationValidationException) {
                MultiStatus validationStatus = ((OrganizationValidationException) e.getCause()).getValidationStatus();
                new MultiStatusDialog(Display.getDefault().getActiveShell(),
                        Messages.organizationValidationFailed,
                        Messages.organizationValidationFailedMsg,
                        MessageDialog.ERROR,
                        new String[] { IDialogConstants.CLOSE_LABEL }, validationStatus).open();

                return Optional.empty();
            }
            BonitaStudioLog.error(e);
            return Optional.of(new Status(IStatus.ERROR, IdentityPlugin.PLUGIN_ID, Messages.publishFailed, e));
        }
        return Optional.of(Status.OK_STATUS);
    }

    protected void updateDefaultUserPreference(ActiveOrganizationProvider activeOrganizationProvider,
            DeployOrganizationControlSupplier controlSupplier) {
        activeOrganizationProvider.saveDefaultUser(controlSupplier.getUsername());
    }

    @CanExecute
    public boolean isEnabled() {
        return RepositoryManager.getInstance().hasActiveRepository();
    }

}
