/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.swtbot.framework.diagram;

import org.bonitasoft.studio.common.ui.jface.SWTBotConstants;
import org.bonitasoft.studio.swtbot.framework.BotBase;
import org.bonitasoft.studio.swtbot.framework.widget.BotTableWidget;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;

/**
 * @author Romain Bioteau
 */
public class BotReportMigrationView extends BotBase implements SWTBotConstants {

    private final BotTableWidget reportTable;

    public BotReportMigrationView(final SWTGefBot bot) {
        super(bot);
        reportTable = new BotTableWidget(bot.tableWithId(SWTBOT_ID_MIGRATION_REPORT_TABLE));
    }

    public int numberOfRows() {
        return reportTable.numberOfRows();
    }

}
