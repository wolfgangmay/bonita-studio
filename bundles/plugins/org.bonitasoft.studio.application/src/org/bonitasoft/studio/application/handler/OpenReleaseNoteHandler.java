/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.studio.application.handler;

import java.net.MalformedURLException;
import java.net.URL;

import org.bonitasoft.studio.common.RedirectURLBuilder;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.preferences.browser.OpenBrowserOperation;
import org.eclipse.e4.core.di.annotations.Execute;

public class OpenReleaseNoteHandler {

    @Execute
    public void openBrowser() {
        try {
            new OpenBrowserOperation(new URL(RedirectURLBuilder.create("696")))
                    .execute();
        } catch (MalformedURLException e) {
            BonitaStudioLog.error(e);
        }
    }
}
