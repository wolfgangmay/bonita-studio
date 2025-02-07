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
package org.bonitasoft.studio.swtbot.framework.organization;

import org.bonitasoft.studio.swtbot.framework.BotBase;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;

public class BotOrganizationOverviewEditor extends BotBase {

    private SWTBotMultiPageEditor editor;

    public BotOrganizationOverviewEditor(SWTGefBot bot, SWTBotMultiPageEditor editor) {
        super(bot);
        this.editor = editor;
    }

    public BotOrganizationOverviewEditor save() {
        editor.save();
        return this;
    }

    public void close() {
        editor.close();
    }

}
