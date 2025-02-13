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
package org.bonitasoft.studio.application.coolbar;

import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.common.extension.IBonitaContributionItem;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.ui.jface.SWTBotConstants;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class DeployCoolbarItem extends ContributionItem
        implements IBonitaContributionItem, ISelectionChangedListener {

    private ToolItem item;
    private Label label;

    private Command getCommand() {
        final ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
        return service.getCommand("org.bonitasoft.studio.application.command.deployArtifacts");
    }

    @Override
    public String getId() {
        return "org.bonitasoft.studio.coolbar.deploy";
    }

    @Override
    public boolean isEnabled() {
        if (RepositoryManager.getInstance().hasActiveRepository()) {
            final Command cmd = getCommand();
            return cmd.isEnabled();
        }
        return false;
    }

    @Override
    public void fill(final ToolBar toolbar, final int index, final int iconSize) {
        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Messages.DeployButtonLabel);
        item.setData(SWTBotConstants.SWTBOT_WIDGET_ID_KEY, SWTBotConstants.SWTBOT_ID_DEPLOY_TOOLITEM);
        if (iconSize < 0) {
            item.setImage(Pics.getImage(PicsConstants.coolbar_deploy_32));
            item.setHotImage(Pics.getImage(PicsConstants.coolbar_deploy_hot_32));
        } else {
            item.setImage(Pics.getImage(PicsConstants.coolbar_deploy_24));
            item.setHotImage(Pics.getImage(PicsConstants.coolbar_deploy_hot_24));
        }
        item.setEnabled(false);
        item.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Command cmd = getCommand();
                try {
                    cmd.executeWithChecks(new ExecutionEvent());
                } catch (final Exception ex) {
                    BonitaStudioLog.error(ex);
                }
            }
        });
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        if (item != null && !item.isDisposed()) {
            item.getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (item != null && !item.isDisposed()) {
                        item.setEnabled(getCommand().isEnabled());
                    }
                }
            });

        }
    }

    @Override
    public String getText() {
        return Messages.DeployButtonLabel;
    }
    
    @Override
    public void setLabelControl(Label label) {
        this.label = label;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(item != null && !item.isDisposed()) {
            item.setEnabled(enabled);
        }
        if(label != null && !label.isDisposed()) {
            label.setEnabled(enabled);
        }
    }

}
