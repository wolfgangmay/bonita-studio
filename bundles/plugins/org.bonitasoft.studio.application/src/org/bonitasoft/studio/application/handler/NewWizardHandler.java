/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.application.handler;

import jakarta.inject.Named;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

public abstract class NewWizardHandler {

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {
        Preferences preferences = ResourcesPlugin.getPlugin()
                .getPluginPreferences();

        preferences.setValue(ResourcesPlugin.PREF_DISABLE_LINKING, true);
        new WizardDialog(Display.getDefault().getActiveShell(), createWizard((IStructuredSelection) selection)).open();
        preferences.setValue(ResourcesPlugin.PREF_DISABLE_LINKING, false);
    }

    protected abstract IWizard createWizard(IStructuredSelection selection);

    @CanExecute
    public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {
        return selection instanceof IStructuredSelection;
    }

}
