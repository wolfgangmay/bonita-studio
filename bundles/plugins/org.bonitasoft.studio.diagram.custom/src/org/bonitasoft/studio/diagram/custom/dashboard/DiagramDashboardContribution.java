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
package org.bonitasoft.studio.diagram.custom.dashboard;

import org.bonitasoft.studio.common.extension.DashboardContribution;
import org.bonitasoft.studio.diagram.custom.i18n.Messages;
import org.bonitasoft.studio.preferences.BonitaThemeConstants;

public class DiagramDashboardContribution implements DashboardContribution {

    @Override
    public Category getCategory() {
        return Category.PROCESS_DATA;
    }

    @Override
    public String getName() {
        return Messages.diagrams;
    }

    @Override
    public String getDescription() {
        return Messages.dashboardDiagramDescription;
    }

    @Override
    public String getColorCssClass() {
        return BonitaThemeConstants.DASHBOARD_PROCESS_BACKGROUND;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
