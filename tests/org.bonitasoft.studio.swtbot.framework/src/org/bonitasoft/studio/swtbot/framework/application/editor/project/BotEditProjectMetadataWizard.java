/**
 * Copyright (C) 2021 BonitaSoft S.A.
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
package org.bonitasoft.studio.swtbot.framework.application.editor.project;

import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.swtbot.framework.BotWizardDialog;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;

public class BotEditProjectMetadataWizard extends BotWizardDialog {

    public BotEditProjectMetadataWizard(SWTGefBot bot) {
        super(bot, Messages.editProjectMetadata, Messages.modify);
    }

    public BotEditProjectMetadataWizard setDescription(String description) {
        bot.textWithLabel(Messages.description).setText(description);
        return this;
    }

    public BotEditProjectMetadataWizard setVersion(String version) {
        bot.textWithLabel(Messages.version + " *").setText(version);
        return this;
    }
    
    @Override
    protected int onFinishTimeout() {
        return 10000;
    }

    public void modify() {
        finish();
    }
    

}
