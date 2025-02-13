/**
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.designer.core.operation;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.designer.core.FormScope;
import org.bonitasoft.studio.designer.core.PageDesignerURLFactory;
import org.bonitasoft.bpm.model.process.Contract;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
public class NewFormOperationFactoryDelegate implements INewFormOperationFactory {

    private static final String NEW_FORM_OPERATION_FACTORY_EXT_ID = "org.bonitasoft.studio.designer.formOperationFactory";

    public CreateUIDArtifactOperation newCreateFormFromContractOperation(PageDesignerURLFactory pageDesignerURLBuilder,
            Contract contract, 
            FormScope formScope, 
            RepositoryAccessor repositoryAccessor) {
        INewFormOperationFactory delegate = findFactory();
        return delegate.newCreateFormFromContractOperation(pageDesignerURLBuilder, contract, formScope, repositoryAccessor);
    }

    private INewFormOperationFactory findFactory() {
        for(IConfigurationElement elem : BonitaStudioExtensionRegistryManager.getInstance().getConfigurationElements(NEW_FORM_OPERATION_FACTORY_EXT_ID)) {
            try {
                return (INewFormOperationFactory) elem.createExecutableExtension("class");
            } catch (CoreException e) {
                BonitaStudioLog.error(e);
            }
        }
        throw new IllegalStateException("No INewFormOperationFactory found.");
    }

}
