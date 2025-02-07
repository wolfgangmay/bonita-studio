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
package org.bonitasoft.studio.configuration.menu;

import org.bonitasoft.studio.common.CommandExecutor;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.configuration.i18n.Messages;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class OpenEnvironmentMenuContribution extends ContributionItem {

    private static final String OPEN_COMMAND = "org.bonitasoft.studio.configuration.command.openEnvironment";

    private CommandExecutor commandExecutor;

    public OpenEnvironmentMenuContribution() {
        commandExecutor = new CommandExecutor();
    }

    @Override
    public void fill(Menu parent, int index) {
        var item = new MenuItem(parent, SWT.NONE, index);
        item.setText(Messages.openEnvironmentsMenuLabel);
        item.addListener(SWT.Selection, e -> commandExecutor.executeCommand(OPEN_COMMAND, null));
        item.setEnabled(isEnabled());
        item.setImage(Pics.getImage(PicsConstants.environment));
        
        parent.addMenuListener(new MenuAdapter() {

            @Override
            public void menuShown(MenuEvent e) {
                item.setEnabled(isEnabled());
            }

        });
    }
    
    @Override
    public boolean isEnabled() {
        if (RepositoryManager.getInstance().hasActiveRepository()) {
            return true;
        }
        return false;
    }

}
