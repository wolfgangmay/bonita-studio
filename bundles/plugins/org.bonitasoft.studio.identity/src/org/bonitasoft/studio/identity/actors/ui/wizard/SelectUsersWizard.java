/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.identity.actors.ui.wizard;

import org.bonitasoft.bpm.model.actormapping.ActorMapping;
import org.bonitasoft.studio.identity.actors.ui.wizard.page.SelectUserMappingWizardPage;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author Romain Bioteau
 *
 */
public class SelectUsersWizard extends Wizard {

    private SelectUserMappingWizardPage page;
    private final ActorMapping workingCopyMapping;
    private final ActorMapping mapping;

    public SelectUsersWizard(ActorMapping mapping){
        setDefaultPageImageDescriptor(Pics.getWizban()) ;
        setForcePreviousAndNextButtons(false) ;
        this.mapping = mapping;
        workingCopyMapping = EcoreUtil.copy(mapping);
    }

    @Override
    public void addPages() {
        page = new SelectUserMappingWizardPage(workingCopyMapping) ;
        addPage(page) ;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        mapping.getUsers().getUser().clear() ;
        for(Object u : page.getSelectedUsers()){
            mapping.getUsers().getUser().add(u.toString());
        }
        return true;
    }

}
