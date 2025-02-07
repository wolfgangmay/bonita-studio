/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.common.repository.ui.viewer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryTreeContentProviderTest {

    @Mock
    IRepositoryStore<?> repoStore;

    @Mock
    IRepositoryFileStore<?> repoFileStore;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(repoFileStore.canBeExported()).thenReturn(true);
    }

    @Test
    void testGetChildren() {
        Mockito.doReturn(Arrays.asList(new IRepositoryFileStore[] { repoFileStore })).when(repoStore).getChildren();
        final Object[] children = new RepositoryTreeContentProvider().getChildren(repoStore);
        Assertions.assertThat(children).contains(repoFileStore);
    }

    @Test
    void testGetChildrenReturnEmptyListIfNotARepositoryStore() {
        final Object[] children = new RepositoryTreeContentProvider().getChildren(new Object());
        Assertions.assertThat(children).isNotNull();
        Assertions.assertThat(children).isEmpty();
    }

    @Test
    void testGetParent() {
        Mockito.doReturn(repoStore).when(repoFileStore).getParentStore();
        Assertions.assertThat(new RepositoryTreeContentProvider().getParent(repoFileStore)).isEqualTo(repoStore);
    }

    @Test
    void testGetParentReturnNullIfNotARepositoryFileStore() {
        Assertions.assertThat(new RepositoryTreeContentProvider().getParent(new Object())).isEqualTo(null);
    }

    @Test
    void should_have_no_child() {
        //Given
        final RepositoryTreeContentProvider repositoryTreeContentProvider = new RepositoryTreeContentProvider();
        doReturn(true).when(repoStore).isEmpty();

        //When Then
        assertThat(repositoryTreeContentProvider.hasChildren(repoStore)).isFalse();
    }

    @Test
    void should_have_children() {
        //Given
        final RepositoryTreeContentProvider repositoryTreeContentProvider = new RepositoryTreeContentProvider();
        doReturn(false).when(repoStore).isEmpty();

        //When Then

        assertThat(repositoryTreeContentProvider.hasChildren(repoStore)).isTrue();
    }

    @Test
    void testHasChildrenReturnFalseIfNotARepositoryFileStore() {
        Assertions.assertThat(new RepositoryTreeContentProvider().hasChildren(new Object())).isFalse();
    }

    @Test
    void testGetElementsReturnATable() {
        final Collection<Object> collec = Collections.emptyList();
        final Object[] elements = new RepositoryTreeContentProvider().getElements(collec);
        Assertions.assertThat(elements).isInstanceOf(Object[].class);

    }

    @Test
    void testGetElementsReturnEmptyListIfNotACollection() {
        final Object[] elements = new RepositoryTreeContentProvider().getElements(new Object());
        Assertions.assertThat(elements).isInstanceOf(Object[].class);
        Assertions.assertThat(elements).isNotNull();
        Assertions.assertThat(elements).isEmpty();
    }

}
