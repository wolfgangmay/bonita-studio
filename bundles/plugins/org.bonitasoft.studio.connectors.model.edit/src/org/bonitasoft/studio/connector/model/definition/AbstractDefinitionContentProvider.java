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
package org.bonitasoft.studio.connector.model.definition;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.bpm.connector.model.definition.Category;
import org.bonitasoft.bpm.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.bpm.connector.model.definition.UnloadableConnectorDefinition;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.provider.DefinitionResourceProvider;
import org.bonitasoft.studio.common.repository.provider.ExtendedCategory;
import org.bonitasoft.studio.common.repository.provider.ExtendedConnectorDefinition;
import org.bonitasoft.studio.connector.model.definition.dialog.DefinitionCategoryContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.framework.Bundle;

/**
 * @author Romain Bioteau
 */
public abstract class AbstractDefinitionContentProvider implements ITreeContentProvider {

    protected final List<ExtendedConnectorDefinition> connectorDefList;
    protected final DefinitionResourceProvider messageProvider;
    protected final String unloadableCategoryName;
    protected DefinitionCategoryContentProvider definitionCategoryContentProvider;
    private final Category unCategorizedCategory;

    protected AbstractDefinitionContentProvider() {
        this(false);
    }

    protected AbstractDefinitionContentProvider(final boolean userDefinitionOnly) {
        final AbstractDefinitionRepositoryStore<?> connectorDefStore = (AbstractDefinitionRepositoryStore<?>) RepositoryManager
                .getInstance().getRepositoryStore(
                        getDefStoreClass());
        connectorDefList = connectorDefStore.getResourceProvider().getConnectorDefinitionRegistry().getDefinitions();
        final Bundle bundle = getBundle();
        messageProvider = DefinitionResourceProvider.getInstance(
                connectorDefStore, bundle);
        definitionCategoryContentProvider = new DefinitionCategoryContentProvider(messageProvider.getAllCategories());
        unloadableCategoryName = messageProvider.getUnloadableCategory().getId();
        unCategorizedCategory = messageProvider.getUncategorizedCategory();
    }

    protected abstract Bundle getBundle();

    protected abstract Class<?> getDefStoreClass();

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer arg0, final Object arg1, final Object arg2) {
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof ExtendedCategory) {
            final ExtendedCategory cat = (ExtendedCategory) element;
            final List<Object> result = new ArrayList<>();
            final String parentId = cat.getId();
            for (final Category c : messageProvider.getAllCategories()) {
                if (parentId.equals(c.getParentCategoryId())) {
                    result.add(c);
                }
            }
            for (final ConnectorDefinition def : connectorDefList) {
                if (def instanceof UnloadableConnectorDefinition) {
                    if (cat.getId().equals(unloadableCategoryName)) {
                        result.add(def);
                    }
                } else {
                    if (def.getCategory().isEmpty()
                            && cat.getId().equals(org.bonitasoft.studio.common.repository.Messages.uncategorized)) {
                        result.add(def);
                    }
                    for (final Category c : def.getCategory()) {
                        if (c.getId().equals(((Category) element).getId())) {
                            if (definitionCategoryContentProvider.isLeafCategory(def, c)) {
                                result.add(def);
                            } else if (def.getCategory().size() == 1) {
                                result.add(def);
                            }
                        }
                    }
                }
            }
            return result.toArray();
        }
        return null;
    }

    @Override
    public Object[] getElements(final Object element) {
        final List<ExtendedCategory> categories = getRootCategories();
        return categories.toArray();
    }

    private List<ExtendedCategory> getRootCategories() {
        final List<ExtendedCategory> categories = new ArrayList<>();
        for (final ExtendedCategory c : messageProvider.getAllCategories()) {
            if (c.getParentCategoryId() == null || c.getParentCategoryId().isEmpty()) {
                categories.add(c);
            }
        }
        return categories;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof ConnectorDefinition) {
            final ConnectorDefinition def = (ConnectorDefinition) element;
            if (def.getCategory().isEmpty()) {
                return unCategorizedCategory;
            }
            for (final Category c : def.getCategory()) {
                if (definitionCategoryContentProvider.isLeafCategory(def, c)) {
                    return messageProvider.getCategory(c);
                }
            }
        } else if (element instanceof Category) {
            final Category category = (Category) element;
            return messageProvider.getParentCategory(category);
        }
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        final Object[] children = getChildren(element);
        return children != null && children.length > 0;
    }

}
